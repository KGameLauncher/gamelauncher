package de.dasbabypixel.gamelauncher.lwjgl.buffers

import de.dasbabypixel.gamelauncher.buffers.ByteBuffer
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

class LWJGLByteBuffer(private val native: MemorySegment) : AbstractBuffer(native.byteSize().toUInt()), ByteBuffer {
    override val address: Long
        get() = position.toLong() + native.address()

    override fun asReadOnly(): LWJGLByteBuffer = this

    override fun put(value: Byte): LWJGLByteBuffer {
        native.set(ValueLayout.JAVA_BYTE, nextPutIndex(), value)
        return this
    }

    override fun put(index: UInt, value: Byte): LWJGLByteBuffer {
        native.set(ValueLayout.JAVA_BYTE, index.toLong(), value)
        return this
    }

    override fun get(): Byte {
        return native.get(ValueLayout.JAVA_BYTE, nextGetIndex())
    }

    override fun get(index: UInt): Byte {
        return native.get(ValueLayout.JAVA_BYTE, index.toLong())
    }

    override fun flip(): LWJGLByteBuffer {
        super.flip()
        return this
    }

    override fun position(newPosition: UInt): LWJGLByteBuffer {
        super.position(newPosition)
        return this
    }

    override fun limit(newLimit: UInt): LWJGLByteBuffer {
        super.limit(newLimit)
        return this
    }

    companion object {
        fun java.nio.ByteBuffer.wrapToByteBuffer() = LWJGLByteBuffer(MemorySegment.ofBuffer(this))
    }
}
