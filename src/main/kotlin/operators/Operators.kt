package de.rki.jfn.operators

internal object Operators : OperatorSet {

    /**
     * All operators set
     */
    override val operators: Set<Operator> = ArrayOperator
        .plus(StringOperator)
        .plus(TimeOperator)
        .plus(MathOperator)
        .plus(AccessingDataOperator)
        .plus(ComparisonOperator)
        .plus(ControlFlowOperator)
        .plus(ExtractionOperator)
        .operators
}
