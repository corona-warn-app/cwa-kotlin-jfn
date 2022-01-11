package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import org.joda.time.*
import org.joda.time.format.ISODateTimeFormat

private enum class TimeUnit(val string: String) {
    SECOND("second"),
    MINUTE( "minute"),
    HOUR("hour"),
    DAY("day"),
    MONTH("month"),
    YEAR("year");
}

private fun String.asTimeUnit(): TimeUnit = when (this) {
    TimeUnit.SECOND.string -> TimeUnit.SECOND
    TimeUnit.MINUTE.string -> TimeUnit.MINUTE
    TimeUnit.HOUR.string -> TimeUnit.HOUR
    TimeUnit.DAY.string -> TimeUnit.DAY
    TimeUnit.MONTH.string -> TimeUnit.MONTH
    TimeUnit.YEAR.string -> TimeUnit.YEAR
    else -> throw IllegalArgumentException("Time unit $this is not supported")
}

// returns the difference between two dates or timestamps in a specific unit of time
internal fun evaluateDiffTime(arguments: List<JsonNode>): JsonNode {
    if (arguments.size != 3) {
        throw IllegalArgumentException("There must be exactly 3 arguments.")
    }

    val left = DateTime.parse(arguments[0].asText())
    val right = DateTime.parse(arguments[1].asText())

    val diff = when (arguments[2].asText().asTimeUnit()) {
        TimeUnit.SECOND -> Seconds.secondsBetween(left, right).seconds
        TimeUnit.MINUTE -> Minutes.minutesBetween(left, right).minutes
        TimeUnit.HOUR -> Hours.hoursBetween(left, right).hours
        TimeUnit.DAY -> Days.daysBetween(left, right).days
        TimeUnit.MONTH -> Months.monthsBetween(left, right).months
        TimeUnit.YEAR -> Years.yearsBetween(left, right).years
    }
    return IntNode.valueOf(diff)
}

// adds a certain amount of a unit of time to a date or timestamp
internal fun evaluatePlusTime(arguments: List<JsonNode>): JsonNode {
    if (arguments.size != 3) {
        throw IllegalArgumentException("There must be exactly 3 arguments.")
    }

    val date = DateTime.parse(arguments[0].asText())
    val amount = arguments[1].asInt()

    val resultDate = when (arguments[2].asText().asTimeUnit()) {
        TimeUnit.SECOND -> date.plusSeconds(amount)
        TimeUnit.MINUTE -> date.plusMinutes(amount)
        TimeUnit.HOUR -> date.plusHours(amount)
        TimeUnit.DAY -> date.plusDays(amount)
        TimeUnit.MONTH -> date.plusMonths(amount)
        TimeUnit.YEAR -> date.plusYears(amount)
    }

    return TextNode(resultDate.toString(ISODateTimeFormat.dateTime()))
}
