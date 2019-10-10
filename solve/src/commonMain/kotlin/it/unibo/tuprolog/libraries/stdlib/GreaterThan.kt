package it.unibo.tuprolog.libraries.stdlib

import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.solve.ExecutionContext

/** Implementation of '>'/2 predicate */
object GreaterThan : ArithmeticRelation<ExecutionContext>(">") {
    override fun arithmeticRelation(x: Numeric, y: Numeric): Boolean =
            x > y
}