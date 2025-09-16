package de.dasbabypixel.gamelauncher.lwjgl.buffers

import de.dasbabypixel.gamelauncher.buffers.IntBuffer
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

class LWJGLIntBuffer(private val native: MemorySegment) : AbstractBuffer(native.byteSize().toUInt()), IntBuffer {
    override val address: Long
        get() = (position.toLong() shl 2) + native.address()

    override fun asReadOnly(): LWJGLIntBuffer = this

    override fun put(value: Int): LWJGLIntBuffer {
        native.set(ValueLayout.JAVA_INT, nextPutIndex() shl 2, value)
        return this
    }

    override fun put(index: UInt, value: Int): LWJGLIntBuffer {
        native.set(ValueLayout.JAVA_INT, index.toLong() shl 2, value)
        return this
    }

    override fun get(): Int {
        return native.get(ValueLayout.JAVA_INT, nextGetIndex() shl 2)
    }

    override fun get(index: UInt): Int {
        return native.get(ValueLayout.JAVA_INT, index.toLong() shl 2)
    }

    override fun flip(): LWJGLIntBuffer {
        super.flip()
        return this
    }

    override fun position(newPosition: UInt): LWJGLIntBuffer {
        super.position(newPosition)
        return this
    }

    override fun limit(newLimit: UInt): LWJGLIntBuffer {
        super.limit(newLimit)
        return this
    }

    companion object {
        fun java.nio.ByteBuffer.wrapToIntBuffer() = LWJGLIntBuffer(MemorySegment.ofBuffer(this))
    }
}
