package it.unibo.tuprolog.solve.problog.lib.primitive

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.primitive.BinaryRelation
import it.unibo.tuprolog.solve.primitive.Solve
import it.unibo.tuprolog.solve.problog.lib.ProblogLib.EXPLANATION_VAR_NAME
import it.unibo.tuprolog.solve.problog.lib.ProblogLib.PREDICATE_PREFIX
import it.unibo.tuprolog.solve.problog.lib.knowledge.ProbExplanation
import it.unibo.tuprolog.solve.problog.lib.knowledge.ProbExplanationTerm
import it.unibo.tuprolog.solve.problog.lib.primitive.ProbSetMode.isPrologMode
import it.unibo.tuprolog.solve.problog.lib.rules.Prob
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

/**
 * This primitive is the core of the probabilistic goal resolution. The first argument is a term representing
 * the [ProbExplanation] explanations of a goal's solutions, and the second argument represents the probabilistic
 * goal itself. The primitive returns a sequence containing all the substitutions for the goal, which are
 * all its solutions and their corresponding explanation in the form of [ProbExplanationTerm].
 *
 * The computation uses regular Prolog semantics. The goal is wrapped in the base level [Prob] predicate in order
 * to be compliant to the way we encode the Problog theory, and then the Prolog resolution engine is used to
 * find all the solutions. There is no way to execute this computation without finding all the solutions of the
 * goal, because solutions with the same substitutions are usually sparse in the solution space generated by a Prolog
 * engine. Once collected all of them, solutions with the same substitution are grouped together. Finally, all the
 * solutions in each group are reduced by applying an "or" operation over their explanation using [ProbExplanation.or].
 * Substitutions and explanations resulting from the reduction of groups are then sequenced together as a result
 * of the probabilistic goal.
 *
 * It is worth mentioning that no probability is calculated by this primitive. Instead, this just bundles the logic
 * for finding probabilistic goal solutions and their explanation.
 *
 * @author Jason Dellaluce
 */
internal object ProbSolve : BinaryRelation.WithoutSideEffects<ExecutionContext>("${PREDICATE_PREFIX}_solve") {

    override fun Solve.Request<ExecutionContext>.computeAllSubstitutions(
        first: Term,
        second: Term
    ): Sequence<Substitution> {
        ensuringArgumentIsInstantiated(1)
        ensuringArgumentIsCallable(1)

        /* Optimize Prolog-only queries */
        if (context.isPrologMode()) {
            return solve(Struct.of(Prob.functor, first, second)).map { it.substitution }
        }

        val explanationVar = Var.of(EXPLANATION_VAR_NAME)
        val solutions = solve(Struct.of(Prob.functor, explanationVar, second)).toList()
        val error = solutions.asSequence().filterIsInstance<Solution.Halt>().firstOrNull()
        if (error != null) throw error.exception

        return if (!solutions.any { s -> s is Solution.Yes }) {
            sequenceOf(Substitution.of(first mguWith ProbExplanationTerm(ProbExplanation.FALSE)))
        } else {
            val solutionGroups = solutions
                .filterIsInstance<Solution.Yes>()
                .groupBy { it.substitution.filter { v, _ -> v != explanationVar } }

            sequence {
                for (solutionGroup in solutionGroups) {
                    val explanation: ProbExplanation = solutionGroup.value
                        .map { v -> v.substitution[explanationVar] }
                        .filterIsInstance<ProbExplanationTerm>()
                        .map { e -> e.explanation }
                        .reduce { acc, expl ->
                            when {
                                expl.probability == 1.0 -> acc
                                acc.probability == 1.0 -> expl
                                else -> acc or expl
                            }
                        }
                    val substitution = Substitution.of(
                        solutionGroup.key,
                        first mguWith ProbExplanationTerm(explanation)
                    )
                    yield(substitution)
                }
            }
        }
    }
}
