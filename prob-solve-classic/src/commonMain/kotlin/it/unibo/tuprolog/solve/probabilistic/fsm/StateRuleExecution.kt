package it.unibo.tuprolog.solve.probabilistic.fsm

import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.prepareForExecution
import it.unibo.tuprolog.solve.probabilistic.ClassicProbabilisticExecutionContext
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith
import it.unibo.tuprolog.utils.Cursor

internal data class StateRuleExecution(override val context: ClassicProbabilisticExecutionContext) : AbstractState(context) {
    private val failureState: StateBacktracking
        get() = StateBacktracking(
            context.copy(rules = Cursor.empty(), step = nextStep())
        )

    override fun computeNext(): State {
        return when (val unifier = context.goals.current!! mguWith context.rules.current!!.head) {
            is Substitution.Unifier -> {
                val newSubstitution = (context.substitution + unifier) as Substitution.Unifier
                val subGoals = context.rules.current!!.prepareForExecution(newSubstitution).body[newSubstitution]

                StateGoalSelection(
                    context.copy(
                        goals = subGoals.toGoals().map{context.representationFactory.from(it)},
                        rules = Cursor.empty(),
                        substitution = newSubstitution,
                        step = nextStep()
                    )
                )
            }
            else -> failureState
        }
    }
}
