package it.unibo.tuprolog.solve.channel.impl

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.utils.dequeOf
import it.unibo.tuprolog.utils.takeFirst
import kotlin.jvm.Synchronized

abstract class AbstractInputChannel<T : Any> : AbstractChannel<T>(), InputChannel<T> {

    private val queue: MutableList<T?> = dequeOf()

    override val available: Boolean
        @Synchronized
        get() = if (isClosed) throw IllegalStateException("Input channel is closed") else true

    protected abstract fun readActually(): T?

    @Synchronized
    final override fun read(): T? {
        if (isClosed) throw IllegalStateException("Input channel is closed")
        refillQueueIfNecessary()
        val read = queue.takeFirst()
        notify(read)
        return read
    }

    private fun refillQueue() {
        queue.add(readActually())
    }

    private fun refillQueueIfNecessary() {
        if (queue.isEmpty()) {
            refillQueue()
        }
    }

    override val isOver: Boolean
        @Synchronized
        get() {
            if (isClosed) throw IllegalStateException("Input channel is closed")
            refillQueueIfNecessary()
            return queue.first() == null
        }

    override fun toString(): String = "${this::class.simpleName}(id=$id, available=$available, closed=$isClosed)"

    override val streamTerm: Struct
        get() = Struct.of("\$stream", Atom.of("in"), Atom.of(id))
}
