package de.rki.jfn

internal fun <T : Comparable<T>> compare(operator: String, args: List<T>): Boolean =
    when (args.size) {
        2 -> intCompare(operator, args[0].compareTo(args[1]), 0)
        3 -> intCompare(operator, args[0].compareTo(args[1]), 0) && intCompare(
            operator,
            args[1].compareTo(args[2]),
            0
        )
        else -> throw RuntimeException("invalid number of operands to a \"$operator\" operation")
    }

internal fun intCompare(operator: String, l: Int, r: Int): Boolean =
    when (operator) {
        "<" -> l < r
        ">" -> l > r
        "<=" -> l <= r
        ">=" -> l >= r
        else -> throw RuntimeException("unhandled comparison operator \"$operator\"")
    }