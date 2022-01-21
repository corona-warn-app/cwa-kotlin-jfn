package de.rki.jfn.operators

object Operators : OperatorSet {

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
