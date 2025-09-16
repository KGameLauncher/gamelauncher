package de.dasbabypixel.gamelauncher.lwjgl.buffers

import de.dasbabypixel.gamelauncher.buffers.FloatBuffer
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.ByteBuffer

class LWJGLFloatBuffer(private val native: MemorySegment) : AbstractBuffer(native.byteSize().toUInt()), FloatBuffer {
    override val address: Long
        get() = (position.toLong() shl 2) + native.address()

    override fun asReadOnly(): LWJGLFloatBuffer = this

    override fun put(value: Float): LWJGLFloatBuffer {
        native.set(ValueLayout.JAVA_FLOAT, nextPutIndex() shl 2, value)
        return this
    }

    override fun put(index: UInt, value: Float): LWJGLFloatBuffer {
        native.set(ValueLayout.JAVA_FLOAT, index.toLong() shl 2, value)
        return this
    }

    override fun get(): Float {
        return native.get(ValueLayout.JAVA_FLOAT, nextGetIndex() shl 2)
    }

    override fun get(index: UInt): Float {
        return native.get(ValueLayout.JAVA_FLOAT, index.toLong() shl 2)
    }

    override fun flip(): LWJGLFloatBuffer {
        super.flip()
        return this
    }

    override fun position(newPosition: UInt): LWJGLFloatBuffer {
        super.position(newPosition)
        return this
    }

    override fun limit(newLimit: UInt): LWJGLFloatBuffer {
        super.limit(newLimit)
        return this
    }

    companion object {
        fun ByteBuffer.wrapToFloatBuffer() = LWJGLFloatBuffer(MemorySegment.ofBuffer(this))
    }
}
