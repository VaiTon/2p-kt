package it.unibo.tuprolog.theory

import it.unibo.tuprolog.theory.testutils.ClauseDatabaseUtils
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test class for [ClauseDatabase.Companion]
 *
 * @author Enrico
 */
internal class ClauseDatabaseTest {

    private val correctInstance = ClauseDatabaseImpl(ClauseDatabaseUtils.wellFormedClauses)

    @Test
    fun ofVarargClauseCreatesCorrectInstance() {
        val toBeTested = ClauseDatabase.of(*ClauseDatabaseUtils.wellFormedClauses.toTypedArray())

        assertEquals(correctInstance, toBeTested)
    }

    @Test
    fun ofIterableClauseCreatesCorrectInstance() {
        val toBeTested = ClauseDatabase.of(ClauseDatabaseUtils.wellFormedClauses)

        assertEquals(correctInstance, toBeTested)
    }

    @Test
    fun ofSequenceClauseCreatesCorrectInstance() {
        val toBeTested = ClauseDatabase.of(ClauseDatabaseUtils.wellFormedClauses.asSequence())

        assertEquals(correctInstance, toBeTested)
    }

}
