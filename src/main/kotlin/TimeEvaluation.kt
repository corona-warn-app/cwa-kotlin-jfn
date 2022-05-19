package de.rki.jfn

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.jfn.error.argError
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/* returns the difference between two dates or timestamps in a specific unit of time */
internal fun evaluateDiffTime(arguments: List<JsonNode>): JsonNode {
    if (arguments.size != 3) argError("There must be exactly 3 arguments.")
    if (!arguments.all { it.isTextual }) argError("All arguments must be textual.")

    val firstDate = arguments[0].asText().parseAsDateTime()
    val secondDate = arguments[1].asText().parseAsDateTime()

    val diff = when (arguments[2].asText().asTimeUnit()) {
        TimeUnit.SECOND -> ChronoUnit.SECONDS.between(secondDate, firstDate)
        TimeUnit.MINUTE -> ChronoUnit.MINUTES.between(secondDate, firstDate)
        TimeUnit.HOUR -> ChronoUnit.HOURS.between(secondDate, firstDate)
        TimeUnit.DAY -> ChronoUnit.DAYS.between(secondDate, firstDate)
        TimeUnit.MONTH -> ChronoUnit.MONTHS.between(
            YearMonth.from(secondDate),
            YearMonth.from(firstDate)
        )
        TimeUnit.YEAR -> ChronoUnit.YEARS.between(secondDate, firstDate)
    }
    return IntNode.valueOf(diff.toInt())
}

/* adds a certain amount of a unit of time to a date or timestamp */
internal fun evaluatePlusTime(arguments: List<JsonNode>): JsonNode {
    if (arguments.size != 3) argError("There must be exactly 3 arguments.")
    if (!arguments[0].isTextual) argError("First argument must be textual.")
    if (!arguments[1].isNumber) argError("Second argument must be a number.")
    if (!arguments[2].isTextual) argError("Third argument must be textual.")

    val date = arguments[0].asText().parseAsDateTime()
    val amount = arguments[1].asLong()

    val resultDate = when (arguments[2].asText().asTimeUnit()) {
        TimeUnit.SECOND -> date.plusSeconds(amount)
        TimeUnit.MINUTE -> date.plusMinutes(amount)
        TimeUnit.HOUR -> date.plusHours(amount)
        TimeUnit.DAY -> date.plusDays(amount)
        TimeUnit.MONTH -> date.plusMonths(amount)
        TimeUnit.YEAR -> date.plusYears(amount)
    }

    return TextNode(resultDate.format(DateTimeFormatter.ISO_DATE_TIME))
}

internal fun evaluateAfter(arguments: List<JsonNode>): BooleanNode {
    if (arguments.size != 2) argError("There must be exactly 2 arguments.")
    if (!arguments.all { it.isTextual }) return BooleanNode.FALSE
    val firstDate = arguments[0].asText().parseAsDateTime()
    val secondDate = arguments[1].asText().parseAsDateTime()

    return BooleanNode.valueOf(
        if (arguments.size == 2) {
            firstDate.isAfter(secondDate)
        } else {
            val thirdDate = arguments[2].asText().parseAsDateTime()
            firstDate.isAfter(secondDate) && secondDate.isAfter(thirdDate)
        }
    )
}

internal fun evaluateNotAfter(arguments: List<JsonNode>): BooleanNode {
    if (arguments.size !in 2..3) argError("There must be exactly 2 or 3 arguments.")
    if (!arguments.all { it.isTextual }) return BooleanNode.TRUE

    val firstDate = arguments[0].asText().parseAsDateTime()
    val secondDate = arguments[1].asText().parseAsDateTime()

    return BooleanNode.valueOf(
        if (arguments.size == 2) {
            !firstDate.isAfter(secondDate)
        } else {
            val thirdDate = arguments[2].asText().parseAsDateTime()
            !firstDate.isAfter(secondDate) && !secondDate.isAfter(thirdDate)
        }
    )
}

internal fun evaluateBefore(arguments: List<JsonNode>): BooleanNode {
    if (arguments.size != 2) argError("There must be exactly 2 arguments.")
    if (!arguments.all { it.isTextual }) return BooleanNode.FALSE

    val firstDate = arguments[0].asText().parseAsDateTime()
    val secondDate = arguments[1].asText().parseAsDateTime()

    return BooleanNode.valueOf(firstDate.isBefore(secondDate))
}

internal fun evaluateNotBefore(arguments: List<JsonNode>): BooleanNode {
    return BooleanNode.valueOf(!evaluateBefore(arguments).asBoolean())
}

private enum class TimeUnit(val unit: String) {
    SECOND("second"),
    MINUTE("minute"),
    HOUR("hour"),
    DAY("day"),
    MONTH("month"),
    YEAR("year");
}

private fun String.asTimeUnit(): TimeUnit =
    TimeUnit.values().find { it.unit == this } ?: argError("Time unit $this is not supported.")

private fun String.parseAsDateTime(): ZonedDateTime = when {
    PATTERN.matches(this) -> ZonedDateTime.parse(this)
    PATTERN_DATE.matches(this) -> LocalDate
        .parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
        .atStartOfDay(ZoneOffset.UTC)
    else -> ZonedDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
}

private val PATTERN = ".*[+|-][0-1]\\d:[0-5]\\d\$".toRegex()
private val PATTERN_DATE = "\\d{4}-\\d{2}-\\d{2}\$".toRegex()
