package de.dasbabypixel.gamelauncher.lwjgl.buffers

import de.dasbabypixel.gamelauncher.buffers.Buffer
import de.dasbabypixel.gamelauncher.buffers.BufferRO
import java.nio.BufferOverflowException
import java.nio.BufferUnderflowException

abstract class AbstractBufferRO(override val capacity: UInt) : BufferRO, LWJGLPointer {
    override var position: UInt = 0u
        protected set
    override val remaining: UInt
        get() {
            if (position >= limit) return 0u
            return limit - position
        }
    override var limit: UInt = 0u
        protected set

    override fun position(newPosition: UInt): AbstractBufferRO {
        if (newPosition > limit) throw IllegalArgumentException("newPosition > limit: ($newPosition > $limit)")
        position = newPosition
        return this
    }

    protected fun nextGetIndex(): Long {
        val p = position
        if (p >= limit) throw BufferUnderflowException()
        position = p + 1u
        return p.toLong()
    }
}

abstract class AbstractBuffer(capacity: UInt) : AbstractBufferRO(capacity), Buffer {
    override fun limit(newLimit: UInt): AbstractBuffer {
        if (newLimit > capacity) throw IllegalArgumentException("newLimit > capacity: ($newLimit > $capacity)")
        limit = newLimit
        if (position > newLimit) position = newLimit
        return this
    }

    override fun position(newPosition: UInt): AbstractBuffer {
        super.position(newPosition)
        return this
    }

    override fun asReadOnly(): AbstractBufferRO = this

    override fun flip(): AbstractBuffer {
        limit = position
        position = 0u
        return this
    }

    protected fun nextPutIndex(): Long {
        val p = position
        if (p >= limit) throw BufferOverflowException()
        position = p + 1u
        return p.toLong()
    }
}