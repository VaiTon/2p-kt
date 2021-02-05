package it.unibo.tuprolog.solve.classic.fsm

import it.unibo.tuprolog.solve.exception.TuPrologRuntimeException
import kotlin.js.JsName

interface ExceptionalState : State {
    @JsName("exception")
    val exception: TuPrologRuntimeException
}
