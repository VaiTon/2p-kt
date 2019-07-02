package it.unibo.tuprolog.core.testutils

import it.unibo.tuprolog.core.Term
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Utils singleton to test equality
 *
 * @author Enrico
 */
internal object AssertionUtils {

    /**
     * Asserts mutual structural equality for two [Term]s
     */
    fun assertStructurallyEquals(expected: Term, actual: Term) {
        assertTrue { expected structurallyEquals actual }
        assertTrue { actual structurallyEquals expected }
    }

    /**
     * Asserts mutual not structural equality for two [Term]s
     */
    fun assertNotStructurallyEquals(expected: Term, actual: Term) {
        assertFalse { expected structurallyEquals actual }
        assertFalse { actual structurallyEquals expected }
    }

    /**
     * Asserts mutual not strict equality for two [Term]s
     */
    fun assertNotStrictlyEquals(expected: Term, actual: Term) {
        assertFalse { expected strictlyEquals actual }
        assertFalse { actual strictlyEquals expected }
    }

    /**
     * Asserts all types of equalities (normal, strict and structural) for two [Term]s
     */
    fun assertEqualities(expected: Term, actual: Term) {
        assertStructurallyEquals(expected, actual)
        assertEquals(expected, actual)
        assertTrue { expected strictlyEquals actual }
        assertTrue { actual strictlyEquals expected }
    }

    /**
     * Asserts not equality of all types (normal, strict and structural) for two [Term]s
     */
    fun assertNoEqualities(expected: Term, actual: Term) {
        assertNotStructurallyEquals(expected, actual)
        assertNotEquals(expected, actual)
        assertNotStrictlyEquals(expected, actual)
    }

    /**
     * Asserts the [assertion] for corresponding items in order
     */
    fun <E> onCorrespondingItems(expected: Iterable<E>, actual: Iterable<E>, assertion: (E, E) -> Unit) =
            expected.zip(actual).forEach { (expected, actual) -> assertion(expected, actual) }

    /**
     * Asserts all types of qualities (normal, strict, and structural) for each [Term] versus all the [Term]s (itself included).
     */
    fun assertAllVsAllEqualities(toBeTested: Iterable<Term>) {
        val toTestItems = toBeTested.count()
        val repeatedElementsSequence = toBeTested.flatMap { testTerm ->
            generateSequence { testTerm }.take(toTestItems).asIterable()
        }
        val repeatedSequenceOfElements = generateSequence { toBeTested }
                .take(toTestItems).flatten().asIterable()

        onCorrespondingItems(repeatedElementsSequence, repeatedSequenceOfElements,
                AssertionUtils::assertEqualities)
    }
}