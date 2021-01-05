package it.unibo.tuprolog.solve.libs.io.primitives

import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.primitive.Solve
import it.unibo.tuprolog.solve.primitive.UnaryPredicate

object PutCode1 : UnaryPredicate.NonBacktrackable<ExecutionContext>("put_code") {
    override fun Solve.Request<ExecutionContext>.computeOne(first: Term): Solve.Response {
        return notImplemented()
    }
}
