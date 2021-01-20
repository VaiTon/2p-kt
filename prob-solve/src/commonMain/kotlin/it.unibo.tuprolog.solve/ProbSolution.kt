package it.unibo.tuprolog.solve

import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.asUnifier
import kotlin.js.JsName

/**
 * Solution to a probabilistic logic query.
 *
 * @author Jason Dellaluce
 * */
data class ProbSolution(
    /** The Prolog logic solution on which the probabilistic solution relies on */
    @JsName("solution")
    val solution: Solution,

    /** The probability distribution for which the logic solution can be considered valid */
    @JsName("probability")
    val probability: Double? = null
) {

    /** Returns a Prolog [Solution] by also encoding the probability in the substitutions */
    fun asSolution(): Solution {
        if (solution !is Solution.Yes) {
            return solution
        }
        var unifier = solution.substitution
        if (probability != null) {
            unifier = unifier.plus(Substitution.of("Probability", Numeric.of(probability))).asUnifier()
        }
        return Solution.yes(solution.query, unifier)
    }
}
