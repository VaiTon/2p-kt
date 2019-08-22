package it.unibo.tuprolog.solve.solver.statemachine

import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.primitive.Signature
import it.unibo.tuprolog.solve.testutils.DummyInstances
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test class for [StateUtils]
 *
 * @author Enrico
 */
internal class StateUtilsTest {

    @Test
    fun orderWithStrategyWithEmptyElementsDoesNothing() {
        assertEquals(
                emptySequence<Nothing>().toList(),
                StateUtils.orderedWithStrategy(emptySequence<Nothing>(), DummyInstances.executionContext) { seq, _ -> seq.first() }.toList()
        )
    }

    @Test
    fun orderWithStrategyPassesCorrectlyParametersToSelectionFunction() {
        StateUtils.orderedWithStrategy(emptySequence<Unit>(), DummyInstances.executionContext) { sequence, context ->
            assertEquals(emptySequence(), sequence)
            assertEquals(DummyInstances.executionContext, context)
        }
    }

    @Test
    fun orderWithStrategyAppliesCorrectlySelectionStrategy() {
        val testSequence = sequenceOf(1, 5, 2, 9, 3, 0, 55)
        val toBeTested = StateUtils.orderedWithStrategy(testSequence, DummyInstances.executionContext) { seq, _ -> seq.min()!! }

        assertEquals(testSequence.sorted().toList(), toBeTested.toList())
    }

    @Test
    fun orderWithStrategyRemovesDuplicatedItems() {
        val testSequence = sequenceOf(1, 5, 2, 9, 3, 0, 5)
        val toBeTested = StateUtils.orderedWithStrategy(testSequence, DummyInstances.executionContext) { seq, _ -> seq.max()!! }

        assertEquals(testSequence.sortedDescending().distinct().toList(), toBeTested.toList())
    }

    @Test
    fun isWellFormedWorksAsExpected() {
        val wellFormedGoal = Struct.of("a", Integer.of(2))
        val nonWellFormedGoal = Tuple.of(Atom.of("a"), Integer.of(3))

        assertEquals(StateUtils.isWellFormed(wellFormedGoal), wellFormedGoal.accept(Clause.bodyWellFormedVisitor))
        assertEquals(StateUtils.isWellFormed(nonWellFormedGoal), nonWellFormedGoal.accept(Clause.bodyWellFormedVisitor))
    }

    @Test
    fun prepareForExecutionWorksAsExpected() {
        val aVar = Var.of("A")
        val bVar = Var.of("B")
        val correct = Tuple.of(Struct.of("call", aVar), Struct.of("call", bVar))

        val toBeTested1 = StateUtils.prepareForExecution(Tuple.of(aVar, bVar))
        val toBeTested2 = StateUtils.prepareForExecution(StateUtils.prepareForExecution(Tuple.of(aVar, bVar)))

        assertEquals(correct, toBeTested1)
        assertEquals(correct, toBeTested2)
    }

    @Test
    fun createNewGoalSolveRequestInjectsPassedParametersInOldRequest() {
        val newGoal = Struct.of("a", Atom.of("ciao"))
        val currentTime = 100L
        val newSubstitution = Substitution.of("A", Atom.of("a"))

        val toBeTested = StateUtils.createNewGoalSolveRequest(DummyInstances.solveRequest, newGoal, currentTime)
        val toBeTestedSubstitution = StateUtils.createNewGoalSolveRequest(DummyInstances.solveRequest, newGoal, currentTime, newSubstitution)

        assertEquals(Signature.fromIndicator(newGoal.indicator), toBeTested.signature)
        assertEquals(newGoal.argsList, toBeTested.arguments)
        assertEquals(listOf(DummyInstances.executionContext), toBeTested.context.parents.toList())
        assertEquals(Substitution.empty(), toBeTested.context.actualSubstitution)

        assertEquals(newSubstitution, toBeTestedSubstitution.context.actualSubstitution)
    }

    @Test
    fun createNewGoalSolveRequestAdjustsCurrentTimeoutIfNotMaxValue() {
        val initialStartTime = 40L
        val initialTimeout = 200L
        val startSolveRequest = with(DummyInstances.solveRequest) {
            copy(executionTimeout = initialTimeout, context = context.copy(computationStartTime = initialStartTime))
        }

        val currentTimeLow = 80L
        val toBeTested = StateUtils.createNewGoalSolveRequest(startSolveRequest, Atom.of("a"), currentTimeLow)

        assertEquals(initialTimeout - (currentTimeLow - initialStartTime), toBeTested.executionTimeout)

        val currentTimeHigh = 350L
        val toBeTestedZeroTimeout = StateUtils.createNewGoalSolveRequest(startSolveRequest, Atom.of("a"), currentTimeHigh)

        assertEquals(0L, toBeTestedZeroTimeout.executionTimeout)
    }

    @Test
    fun createNewGoalSolveRequestDoesntAdjustTimeOutIfMaxValue() {
        val toBeTested = StateUtils.createNewGoalSolveRequest(
                DummyInstances.solveRequest.copy(executionTimeout = Long.MAX_VALUE),
                Atom.of("a"),
                100L
        )

        assertEquals(Long.MAX_VALUE, toBeTested.executionTimeout)
    }
}
