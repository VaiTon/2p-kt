package it.unibo.tuprolog.solve.libs.oop.exceptions

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.exception.PrologError
import it.unibo.tuprolog.solve.exception.error.ExistenceError
import it.unibo.tuprolog.solve.libs.oop.fullName
import it.unibo.tuprolog.solve.libs.oop.name
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate")
class MethodInvocationException(
    val type: KClass<*>,
    val missingMethodName: String,
    val admissibleTypes: List<Set<KClass<*>>>
) : OopException(
    "There is no method on type ${type.fullName} which is named `$missingMethodName` and accepts " +
        "[${admissibleTypes.pretty()}] as formal arguments"
) {
    override fun toPrologError(
        context: ExecutionContext,
        signature: Signature
    ): PrologError {
        return ExistenceError.of(
            context,
            ExistenceError.ObjectType.OOP_METHOD,
            culprit,
            message ?: ""
        )
    }

    override val culprit: Term
        get() = Atom.of("${type.fullName}::$missingMethodName(${admissibleTypes.pretty()})")
}
