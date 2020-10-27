package it.unibo.tuprolog.theory.impl

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.theory.AbstractTheory
import it.unibo.tuprolog.theory.MutableTheory
import it.unibo.tuprolog.theory.RetractResult
import it.unibo.tuprolog.theory.TheoryUtils.checkClausesCorrect
import it.unibo.tuprolog.unify.Unificator.Companion.matches
import it.unibo.tuprolog.utils.dequeOf
import kotlin.collections.List as KtList

internal class ListedTheory
private constructor(
    clauses: KtList<Clause>
) : AbstractListedTheory(clauses) {

    constructor(clauses: Iterable<Clause>) : this(clauses.toList()) {
        checkClausesCorrect(clauses)
    }

    constructor(clauses: Sequence<Clause>) : this(clauses.toList()) {
        checkClausesCorrect(clauses)
    }

    override fun createNewTheory(clauses: Sequence<Clause>): AbstractTheory {
        return ListedTheory(clauses)
    }

    override fun retract(clause: Clause): RetractResult<ListedTheory> {
        val retractability = clauses.filter { it matches clause }
        return when {
            retractability.none() -> RetractResult.Failure(this)
            else -> {
                val toBeActuallyRetracted = retractability.first()
                val newTheory = clauses.filter { it != toBeActuallyRetracted }
                RetractResult.Success(
                    ListedTheory(
                        newTheory
                    ),
                    listOf(toBeActuallyRetracted)
                )
            }
        }
    }

    override fun toMutableTheory(): MutableTheory {
        return MutableTheory.listedOf(this)
    }

    override fun retract(clauses: Iterable<Clause>): RetractResult<ListedTheory> {
        val residual = dequeOf(this.clauses)
        val removed = dequeOf<Clause>()
        val i = residual.iterator()
        while (i.hasNext()) {
            val current = i.next()
            if (clauses.any { it matches current }) {
                i.remove()
                removed.add(current)
            }
        }
        return if (removed.isEmpty()) {
            RetractResult.Failure(this)
        } else {
            RetractResult.Success(ListedTheory(residual), removed)
        }
    }

    override fun retractAll(clause: Clause): RetractResult<ListedTheory> {
        val retractability = clauses.filter { it matches clause }
        return when {
            retractability.none() -> RetractResult.Failure(this)
            else -> {
                val partitionedClauses = clauses.toList().partition { it matches clause }
                val newTheory = partitionedClauses.second
                val toBeActuallyRetracted = partitionedClauses.first
                RetractResult.Success(
                    ListedTheory(
                        newTheory
                    ),
                    toBeActuallyRetracted
                )
            }
        }
    }

    private val hashCodeCache: Int by lazy {
        super.hashCode()
    }

    override fun hashCode(): Int {
        return hashCodeCache
    }

    private val sizeCache: Long by lazy {
        super.size
    }

    override val size: Long
        get() = sizeCache
}
