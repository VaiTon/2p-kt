package it.unibo.tuprolog.unify

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Substitution.Companion.asUnifier
import it.unibo.tuprolog.core.Substitution.Companion.failed
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.unify.Equation.*

abstract class AbstractUnificationStrategy : Unification {

    private val _context: Iterable<Equation<Var, Term>>

    constructor(context: Iterable<Equation<Var, Term>> = emptyList()) {
        _context = context
        for (eq in context) {
            if (eq is Contradiction) {
                throw IllegalArgumentException("Invalid equation in context: $eq")
            }
        }
    }

    override val context: Substitution
        get() = _context.map { it.toPair() }.toMap().asUnifier()


    protected abstract fun checkTermsEquality(first: Term, second: Term): Boolean

    private val termsEqualityChecker: (Term, Term)->Boolean = { a, b -> checkTermsEquality(a, b) }

    protected fun occurrenceCheck(variable: Var, term: Term): Boolean {
        return when (term) {
            is Var -> checkTermsEquality(variable, term)
            is Struct -> term.args.any { occurrenceCheck(variable, it) }
            else -> false
        }
    }

    private fun applySubstitutionToEquations(substitution: Substitution, equations: MutableList<Equation<Term, Term>>,
                                             exceptIndex: Int): Boolean {

        var changed = false

        for (i in equations.indices) {
            if (i == exceptIndex || equations[i] is Contradiction || equations[i] is Identity) continue

            with(equations[i]) {
                val newLhs = lhs[substitution]
                val newRhs = rhs[substitution]
                if (lhs !== newLhs || rhs !== newRhs) {
                    equations[i] = Equation.of(newLhs, newRhs, termsEqualityChecker)
                    changed = true
                }
            }
        }

        return changed
    }

    protected fun equationsFor(term1: Term, term2: Term): Sequence<Equation<Term, Term>> =
        Equation.allOf(term1, term2, termsEqualityChecker)


    override fun mgu(term1: Term, term2: Term, occurCheckEnabled: Boolean): Substitution {
        val equations = (_context + equationsFor(term1, term2)).toMutableList()

        var changed = true

        while (changed) {

            changed = false

            val i = equations.listIterator()

            while (i.hasNext()) {

                i.next().also {
                    when (it) {
                        is Contradiction -> {
                            return failed()
                        }
                        is Identity -> {
                            i.remove()
                            changed = true
                        }
                        is Assignment -> {
                            if (occurCheckEnabled && occurrenceCheck(it.lhs as Var, it.rhs)) {
                                return failed()
                            } else {
                                changed = applySubstitutionToEquations(Substitution.of(it.lhs as Var, it.rhs), equations, i.previousIndex())
                            }
                        }
                        is Comparison -> {
                            i.remove()
                            for (eq in equationsFor(it.lhs, it.rhs)) {
                                i.add(eq)
                            }
                            changed = true
                        }
                    }
                }
            }
        }

        return equations.filterIsInstance<Assignment<Var, Term>>().toSubstitution()
    }
}