package it.unibo.tuprolog.solve

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.ExecutionContextAware.Companion.STDERR
import it.unibo.tuprolog.solve.ExecutionContextAware.Companion.STDIN
import it.unibo.tuprolog.solve.ExecutionContextAware.Companion.STDOUT
import it.unibo.tuprolog.solve.ExecutionContextAware.Companion.WARNINGS
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.exception.PrologWarning
import it.unibo.tuprolog.solve.library.Libraries
import it.unibo.tuprolog.solve.fsm.EndState
import it.unibo.tuprolog.solve.fsm.State
import it.unibo.tuprolog.solve.fsm.StateInit
import it.unibo.tuprolog.theory.ClauseDatabase

internal class ClassicSolver(
    libraries: Libraries = Libraries(),
    flags: PrologFlags = emptyMap(),
    staticKB: ClauseDatabase = ClauseDatabase.empty(),
    dynamicKB: ClauseDatabase = ClauseDatabase.empty(),
    inputChannels: PrologInputChannels<*> = ExecutionContextAware.defaultInputChannels(),
    outputChannels: PrologOutputChannels<*> = ExecutionContextAware.defaultOutputChannels()
) : Solver {

    private var state: State = StateInit(
        ExecutionContextImpl(
            libraries = libraries,
            flags = flags,
            staticKB = staticKB,
            dynamicKB = dynamicKB,
            inputChannels = inputChannels,
            outputChannels = outputChannels
        )
    )

    private fun Substitution.Unifier.cleanUp(): Substitution.Unifier {
        return filter { _, term -> term !is Var }
    }

    private fun Solution.Yes.cleanUp(): Solution.Yes {
        return copy(substitution = substitution.cleanUp())
    }

    override fun solve(goal: Struct, maxDuration: TimeDuration): Sequence<Solution> = sequence {
        val initialContext = ExecutionContextImpl(
            query = goal,
            libraries = libraries,
            flags = flags,
            staticKB = staticKB,
            dynamicKB = dynamicKB,
            inputChannels = inputChannels,
            outputChannels = outputChannels,
            maxDuration = maxDuration,
            startTime = currentTimeInstant()
        )

        state = StateInit(initialContext)
        var step: Long = 0

        while (true) {
            require(state.context.step == step)
            state = state.next()
            step += 1

            if (state is EndState) {
                val endState = state as EndState
                yield(
                    when (val sol = endState.solution) {
                        is Solution.Yes -> sol.cleanUp()
                        else -> sol
                    }
                )

                if (!endState.hasOpenAlternatives) break
            }
        }
    }

    override val libraries: Libraries
        get() = state.context.libraries

    override val flags: PrologFlags
        get() = state.context.flags

    override val staticKB: ClauseDatabase
        get() = state.context.staticKB

    override val dynamicKB: ClauseDatabase
        get() = state.context.dynamicKB

    override val inputChannels: PrologInputChannels<*>
        get() = state.context.inputChannels

    override val outputChannels: PrologOutputChannels<*>
        get() = state.context.outputChannels
}