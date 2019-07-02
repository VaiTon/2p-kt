package it.unibo.tuprolog.core.integrationtests

import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.core.testutils.AssertionUtils.assertAllVsAllEqualities
import it.unibo.tuprolog.core.testutils.AssertionUtils.assertNoEqualities
import it.unibo.tuprolog.core.testutils.TermTypeAssertionUtils.assertIsTruth
import kotlin.test.Test
import kotlin.test.assertTrue

class TestTrue {

    private val correctAtom = "true"
    private val notCorrectAtom = "true "

    private val heterogeneousCreatedInstances = listOf(
            Truth.`true`(),
            Truth.of(true),
            Atom.of(correctAtom),
            atomOf(correctAtom),
            Struct.of(correctAtom),
            structOf(correctAtom))

    @Test
    fun variousCreationMethodsCreateCorrectlyTrue() {
        heterogeneousCreatedInstances.forEach {
            assertIsTruth(it)
            assertTrue(it.isTrue)
        }
    }

    @Test
    fun equality() {
        assertAllVsAllEqualities(heterogeneousCreatedInstances)

        val notTrueAtom = Atom.of(notCorrectAtom)
        heterogeneousCreatedInstances.forEach { correct -> assertNoEqualities(notTrueAtom, correct) }
    }
}