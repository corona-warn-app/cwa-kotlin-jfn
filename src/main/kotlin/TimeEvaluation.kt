package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import org.joda.time.Months
import org.joda.time.Seconds
import org.joda.time.Years
import org.joda.time.format.ISODateTimeFormat

private enum class TimeUnit(val string: String) {
    SECOND("second"),
    MINUTE("minute"),
    HOUR("hour"),
    DAY("day"),
    MONTH("month"),
    YEAR("year");
}

private fun String.asTimeUnit(): TimeUnit =
    TimeUnit.values().find { it.string == this }
        ?: throw IllegalArgumentException("Time unit $this is not supported.")

/* returns the difference between two dates or timestamps in a specific unit of time */
internal fun evaluateDiffTime(arguments: List<JsonNode>): JsonNode {
    if (arguments.size != 3) {
        throw IllegalArgumentException("There must be exactly 3 arguments.")
    }

    if (!arguments.all { it.isTextual }) {
        throw IllegalArgumentException("All arguments must be textual.")
    }

    val firstDate = arguments[0].asText().parseAsDateTimeUtc()
    val secondDate = arguments[1].asText().parseAsDateTimeUtc()

    val diff = when (arguments[2].asText().asTimeUnit()) {
        TimeUnit.SECOND -> Seconds.secondsBetween(secondDate, firstDate).seconds
        TimeUnit.MINUTE -> Minutes.minutesBetween(secondDate, firstDate).minutes
        TimeUnit.HOUR -> Hours.hoursBetween(secondDate, firstDate).hours
        TimeUnit.DAY -> Days.daysBetween(secondDate, firstDate).days
        TimeUnit.MONTH -> Months.monthsBetween(secondDate, firstDate).months
        TimeUnit.YEAR -> Years.yearsBetween(secondDate, firstDate).years
    }
    return IntNode.valueOf(diff)
}

/* adds a certain amount of a unit of time to a date or timestamp */
internal fun evaluatePlusTime(arguments: List<JsonNode>): JsonNode {
    if (arguments.size != 3) {
        throw IllegalArgumentException("There must be exactly 3 arguments.")
    }

    if (!arguments[0].isTextual) {
        throw IllegalArgumentException("First argument must be textual.")
    }

    if (!arguments[1].isNumber) {
        throw IllegalArgumentException("Second argument must be a number.")
    }

    if (!arguments[2].isTextual) {
        throw IllegalArgumentException("Third argument must be textual.")
    }

    val date = arguments[0].asText().parseAsDateTimeUtc()
    val amount = arguments[1].asInt()

    val resultDate = when (arguments[2].asText().asTimeUnit()) {
        TimeUnit.SECOND -> date.plusSeconds(amount)
        TimeUnit.MINUTE -> date.plusMinutes(amount)
        TimeUnit.HOUR -> date.plusHours(amount)
        TimeUnit.DAY -> date.plusDays(amount)
        TimeUnit.MONTH -> date.plusMonths(amount)
        TimeUnit.YEAR -> date.plusYears(amount)
    }.toDateTime(date.zone)

    return TextNode(resultDate.toString(ISODateTimeFormat.dateTimeNoMillis()))
}

internal fun evaluateAfter(arguments: List<JsonNode>): BooleanNode {
    if (arguments.size != 2) {
        throw IllegalArgumentException("There must be exactly 2 arguments.")
    }

    if (!arguments.all { it.isTextual }) {
        throw IllegalArgumentException("All arguments must be textual.")
    }

    val firstDate = arguments[0].asText().parseAsDateTimeUtc()
    val secondDate = arguments[1].asText().parseAsDateTimeUtc()

    return BooleanNode.valueOf(firstDate.isAfter(secondDate))
}

internal fun evaluateNotAfter(arguments: List<JsonNode>): BooleanNode {
    return BooleanNode.valueOf(!evaluateAfter(arguments).asBoolean())
}

internal fun evaluateBefore(arguments: List<JsonNode>): BooleanNode {
    if (arguments.size != 2) {
        throw IllegalArgumentException("There must be exactly 2 arguments.")
    }

    if (!arguments.all { it.isTextual }) {
        throw IllegalArgumentException("All arguments must be textual.")
    }

    val firstDate = arguments[0].asText().parseAsDateTimeUtc()
    val secondDate = arguments[1].asText().parseAsDateTimeUtc()

    return BooleanNode.valueOf(firstDate.isBefore(secondDate))
}

internal fun evaluateNotBefore(arguments: List<JsonNode>): BooleanNode {
    return BooleanNode.valueOf(!evaluateBefore(arguments).asBoolean())
}

private fun String.parseAsDateTimeUtc(): DateTime {
    return if (pattern.matches(this))
        DateTime.parse(this, ISODateTimeFormat.dateTimeParser())
    else
        DateTime.parse(this, ISODateTimeFormat.dateTimeParser().withZoneUTC())
}

private const val TIME_ZONE_REGEX = ".*[+|-][0-1][0-9]:[0-5][0-9]\$"
private val pattern = Regex(TIME_ZONE_REGEX)
