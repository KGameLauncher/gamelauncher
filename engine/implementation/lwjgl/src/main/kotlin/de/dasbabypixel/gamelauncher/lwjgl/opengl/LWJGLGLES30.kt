package de.dasbabypixel.gamelauncher.lwjgl.opengl

import de.dasbabypixel.gamelauncher.buffers.*
import de.dasbabypixel.gamelauncher.lwjgl.buffers.LWJGLByteBuffer.Companion.wrapToByteBuffer
import org.lwjgl.opengles.GLES30

abstract class LWJGLGLES30 : LWJGLGLES20(), de.dasbabypixel.gamelauncher.gles.GLES30 {
    override fun glBeginQuery(target: Int, id: UInt) = GLES30.glBeginQuery(target, id.toInt())

    override fun glBeginTransformFeedback(primitiveMode: Int) = GLES30.glBeginTransformFeedback(primitiveMode)

    override fun glBindBufferBase(target: Int, index: UInt, buffer: UInt) =
        GLES30.glBindBufferBase(target, index.toInt(), buffer.toInt())

    override fun glBindBufferRange(target: Int, index: UInt, buffer: UInt, offset: Long, size: ULong) =
        GLES30.glBindBufferRange(target, index.toInt(), buffer.toInt(), offset, size.toLong())

    override fun glBindSampler(unit: UInt, sampler: UInt) = GLES30.glBindSampler(unit.toInt(), sampler.toInt())

    override fun glBindTransformFeedback(target: Int, id: UInt) = GLES30.glBindTransformFeedback(target, id.toInt())

    override fun glBindVertexArray(array: UInt) = GLES30.glBindVertexArray(array.toInt())

    override fun glBlitFramebuffer(
        srcX0: Int,
        srcY0: Int,
        srcX1: Int,
        srcY1: Int,
        dstX0: Int,
        dstY0: Int,
        dstX1: Int,
        dstY1: Int,
        mask: Int,
        filter: Int
    ) = GLES30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter)

    override fun glClearBufferfi(buffer: Int, drawBuffer: Int, depth: Float, stencil: Int) =
        GLES30.glClearBufferfi(buffer, drawBuffer, depth, stencil)

    override fun glClearBufferfv(buffer: Int, drawBuffer: Int, value: FloatBufferRO) =
        GLES30.nglClearBufferfv(buffer, drawBuffer, value.address)

    override fun glClearBufferiv(buffer: Int, drawBuffer: Int, value: IntBufferRO) =
        GLES30.nglClearBufferiv(buffer, drawBuffer, value.address)

    override fun glClearBufferuiv(buffer: Int, drawBuffer: Int, value: IntBufferRO) =
        GLES30.nglClearBufferuiv(buffer, drawBuffer, value.address)

    override fun glClientWaitSync(sync: Long, flags: Int, timeout: ULong): Int =
        GLES30.nglClientWaitSync(sync, flags, timeout.toLong())

    override fun glCompressedTexImage3D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: UInt,
        height: UInt,
        depth: UInt,
        border: Int,
        imageSize: UInt,
        data: BufferRO
    ) = GLES30.nglCompressedTexImage3D(
        target,
        level,
        internalformat,
        width.toInt(),
        height.toInt(),
        depth.toInt(),
        border,
        imageSize.toInt(),
        data.address
    )

    override fun glCompressedTexSubImage3D(
        target: Int,
        level: Int,
        xoffset: Int,
        yoffset: Int,
        zoffset: Int,
        width: UInt,
        height: UInt,
        depth: UInt,
        format: Int,
        imageSize: UInt,
        data: BufferRO
    ) = GLES30.nglCompressedTexSubImage3D(
        target,
        level,
        xoffset,
        yoffset,
        zoffset,
        width.toInt(),
        height.toInt(),
        depth.toInt(),
        format,
        imageSize.toInt(),
        data.address
    )

    override fun glCopyBufferSubData(
        readtarget: Int, writetarget: Int, readoffset: Long, writeoffset: Long, size: ULong
    ) = GLES30.glCopyBufferSubData(readtarget, writetarget, readoffset, writeoffset, size.toLong())

    override fun glCopyTexSubImage3D(
        target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, x: Int, y: Int, width: UInt, height: UInt
    ) = GLES30.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width.toInt(), height.toInt())

    override fun glDeleteQueries(n: UInt, ids: IntBufferRO) = GLES30.nglDeleteQueries(n.toInt(), ids.address)

    override fun glDeleteSamplers(n: UInt, samplers: IntBufferRO) =
        GLES30.nglDeleteSamplers(n.toInt(), samplers.address)

    override fun glDeleteSync(sync: Long) = GLES30.glDeleteSync(sync)

    override fun glDeleteTransformFeedbacks(n: UInt, ids: IntBufferRO) =
        GLES30.nglDeleteTransformFeedbacks(n.toInt(), ids.address)

    override fun glDeleteVertexArrays(n: UInt, arrays: IntBufferRO) =
        GLES30.nglDeleteVertexArrays(n.toInt(), arrays.address)

    override fun glDrawArraysInstanced(mode: Int, first: Int, count: UInt, primcount: UInt) =
        GLES30.glDrawArraysInstanced(mode, first, count.toInt(), primcount.toInt())

    override fun glDrawBuffers(n: UInt, bufs: IntBufferRO) = GLES30.nglDrawBuffers(n.toInt(), bufs.address)

    override fun glDrawElementsInstanced(mode: Int, count: UInt, type: Int, indices: BufferRO, primcount: UInt) =
        GLES30.nglDrawElementsInstanced(mode, count.toInt(), type, indices.address, primcount.toInt())

    override fun glDrawRangeElements(mode: Int, start: UInt, end: UInt, count: UInt, type: Int, indices: BufferRO) =
        GLES30.nglDrawRangeElements(mode, start.toInt(), end.toInt(), count.toInt(), type, indices.address)

    override fun glEndQuery(target: Int) = GLES30.glEndQuery(target)

    override fun glEndTransformFeedback() = GLES30.glEndTransformFeedback()

    override fun glFenceSync(condition: Int, flags: Int): Long = GLES30.glFenceSync(condition, flags)

    override fun glFlushMappedBufferRange(target: Int, offset: Long, length: ULong) =
        GLES30.glFlushMappedBufferRange(target, offset, length.toLong())

    override fun glFramebufferTextureLayer(target: Int, attachment: Int, texture: UInt, level: Int, layer: Int) =
        GLES30.glFramebufferTextureLayer(target, attachment, texture.toInt(), level, layer)

    override fun glGenQueries(n: UInt, ids: IntBuffer) = GLES30.nglGenQueries(n.toInt(), ids.address)

    override fun glGenSamplers(n: UInt, samplers: IntBuffer) = GLES30.nglGenSamplers(n.toInt(), samplers.address)

    override fun glGenTransformFeedbacks(n: UInt, ids: IntBuffer) =
        GLES30.nglGenTransformFeedbacks(n.toInt(), ids.address)

    override fun glGenVertexArrays(n: UInt, arrays: IntBuffer) = GLES30.nglGenVertexArrays(n.toInt(), arrays.address)

    override fun glGetActiveUniformBlockName(
        program: UInt, uniformBlockIndex: UInt, bufSize: UInt, length: IntBuffer, uniformBlockName: ByteBuffer
    ) = GLES30.nglGetActiveUniformBlockName(
        program.toInt(), uniformBlockIndex.toInt(), bufSize.toInt(), length.address, uniformBlockName.address
    )

    override fun glGetActiveUniformBlockiv(program: UInt, uniformBlockIndex: UInt, pname: Int, params: IntBuffer) =
        GLES30.nglGetActiveUniformBlockiv(program.toInt(), uniformBlockIndex.toInt(), pname, params.address)

    override fun glGetActiveUniformsiv(
        program: UInt, uniformCount: UInt, uniformIndices: IntBufferRO, pname: Int, params: IntBuffer
    ) = GLES30.nglGetActiveUniformsiv(
        program.toInt(), uniformCount.toInt(), uniformIndices.address, pname, params.address
    )

    override fun glGetBufferParameteri64v(target: Int, value: Int, data: LongBuffer) =
        GLES30.nglGetBufferParameteri64v(target, value, data.address)

    override fun glGetBufferPointerv(target: Int, pname: Int, params: PointerBuffer) =
        GLES30.nglGetBufferPointerv(target, pname, params.address)

    override fun glGetFragDataLocation(program: UInt, name: String?): Int =
        GLES30.glGetFragDataLocation(program.toInt(), name!!)

    override fun glGetInteger64i_v(target: Int, index: UInt, data: LongBuffer) =
        GLES30.nglGetInteger64i_v(target, index.toInt(), data.address)

    override fun glGetInteger64v(pname: Int, data: LongBuffer) = GLES30.nglGetInteger64v(pname, data.address)

    override fun glGetIntegeri_v(target: Int, index: UInt, data: IntBuffer) =
        GLES30.nglGetIntegeri_v(target, index.toInt(), data.address)

    override fun glGetInternalformativ(target: Int, internalformat: Int, pname: Int, bufSize: UInt, params: IntBuffer) =
        GLES30.nglGetInternalformativ(target, internalformat, pname, bufSize.toInt(), params.address)

    override fun glGetProgramBinary(
        program: UInt, bufsize: UInt, length: IntBuffer, binaryFormat: IntBuffer, binary: Buffer
    ) = GLES30.nglGetProgramBinary(
        program.toInt(), bufsize.toInt(), length.address, binaryFormat.address, binary.address
    )

    override fun glGetQueryObjectuiv(id: UInt, pname: Int, params: IntBuffer) =
        GLES30.nglGetQueryObjectuiv(id.toInt(), pname, params.address)

    override fun glGetQueryiv(target: Int, pname: Int, params: IntBuffer) =
        GLES30.nglGetQueryiv(target, pname, params.address)

    override fun glGetSamplerParameterfv(sampler: UInt, pname: Int, params: FloatBuffer) =
        GLES30.nglGetSamplerParameterfv(sampler.toInt(), pname, params.address)

    override fun glGetSamplerParameteriv(sampler: UInt, pname: Int, params: IntBuffer) =
        GLES30.nglGetSamplerParameteriv(sampler.toInt(), pname, params.address)

    override fun glGetStringi(name: Int, index: UInt): String? = GLES30.glGetStringi(name, index.toInt())

    override fun glGetSynciv(sync: Long, pname: Int, bufSize: UInt, length: IntBuffer, values: IntBuffer) =
        GLES30.nglGetSynciv(sync, pname, bufSize.toInt(), length.address, values.address)

    override fun glGetTransformFeedbackVarying(
        program: UInt, index: UInt, bufSize: UInt, length: IntBuffer, size: IntBuffer, type: IntBuffer, name: ByteBuffer
    ) = GLES30.nglGetTransformFeedbackVarying(
        program.toInt(), index.toInt(), bufSize.toInt(), length.address, size.address, type.address, name.address
    )

    override fun glGetUniformBlockIndex(program: UInt, uniformBlockName: String?): UInt =
        GLES30.glGetUniformBlockIndex(program.toInt(), uniformBlockName!!).toUInt()

    override fun glGetUniformIndices(
        program: UInt, uniformCount: UInt, uniformNames: PointerBuffer, uniformIndices: IntBuffer
    ) = GLES30.nglGetUniformIndices(program.toInt(), uniformCount.toInt(), uniformNames.address, uniformIndices.address)

    override fun glGetUniformuiv(program: UInt, location: Int, params: IntBuffer) =
        GLES30.nglGetUniformuiv(program.toInt(), location, params.address)

    override fun glGetVertexAttribIiv(index: UInt, pname: Int, params: IntBuffer) =
        GLES30.nglGetVertexAttribIiv(index.toInt(), pname, params.address)

    override fun glGetVertexAttribIuiv(index: UInt, pname: Int, params: IntBuffer) =
        GLES30.nglGetVertexAttribIuiv(index.toInt(), pname, params.address)

    override fun glInvalidateFramebuffer(target: Int, numAttachments: UInt, attachments: IntBufferRO) =
        GLES30.nglInvalidateFramebuffer(target, numAttachments.toInt(), attachments.address)

    override fun glInvalidateSubFramebuffer(
        target: Int, numAttachments: UInt, attachments: IntBufferRO, x: Int, y: Int, width: UInt, height: UInt
    ) = GLES30.nglInvalidateSubFramebuffer(
        target, numAttachments.toInt(), attachments.address, x, y, width.toInt(), height.toInt()
    )

    override fun glIsQuery(id: UInt): Boolean = GLES30.glIsQuery(id.toInt())

    override fun glIsSampler(id: UInt): Boolean = GLES30.glIsSampler(id.toInt())

    override fun glIsSync(sync: Long): Boolean = GLES30.glIsSync(sync)

    override fun glIsTransformFeedback(id: UInt): Boolean = GLES30.glIsTransformFeedback(id.toInt())

    override fun glIsVertexArray(array: UInt): Boolean = GLES30.glIsVertexArray(array.toInt())

    override fun glMapBufferRange(target: Int, offset: Long, length: ULong, access: Int): Buffer =
        GLES30.glMapBufferRange(target, offset, length.toLong(), access)!!.wrapToByteBuffer()

    override fun glPauseTransformFeedback() = GLES30.glPauseTransformFeedback()

    override fun glProgramBinary(program: UInt, binaryFormat: Int, binary: BufferRO, length: UInt) =
        GLES30.nglProgramBinary(program.toInt(), binaryFormat, binary.address, length.toInt())

    override fun glProgramParameteri(program: UInt, pname: Int, value: Int) =
        GLES30.glProgramParameteri(program.toInt(), pname, value)

    override fun glReadBuffer(src: Int) = GLES30.glReadBuffer(src)

    override fun glRenderbufferStorageMultisample(
        target: Int, samples: UInt, internalformat: Int, width: UInt, height: UInt
    ) = GLES30.glRenderbufferStorageMultisample(target, samples.toInt(), internalformat, width.toInt(), height.toInt())

    override fun glResumeTransformFeedback() = GLES30.glResumeTransformFeedback()

    override fun glSamplerParameterf(sampler: UInt, pname: Int, param: Float) =
        GLES30.glSamplerParameterf(sampler.toInt(), pname, param)

    override fun glSamplerParameterfv(sampler: UInt, pname: Int, params: FloatBufferRO) =
        GLES30.nglSamplerParameterfv(sampler.toInt(), pname, params.address)

    override fun glSamplerParameteri(sampler: UInt, pname: Int, param: Int) =
        GLES30.glSamplerParameteri(sampler.toInt(), pname, param)

    override fun glSamplerParameteriv(sampler: UInt, pname: Int, params: IntBufferRO) =
        GLES30.nglSamplerParameteriv(sampler.toInt(), pname, params.address)

    override fun glTexImage3D(
        target: Int,
        level: Int,
        internalFormat: Int,
        width: UInt,
        height: UInt,
        depth: UInt,
        border: Int,
        format: Int,
        type: Int,
        data: BufferRO
    ) = GLES30.nglTexImage3D(
        target, level, internalFormat, width.toInt(), height.toInt(), depth.toInt(), border, format, type, data.address
    )

    override fun glTexStorage2D(target: Int, levels: UInt, internalformat: Int, width: UInt, height: UInt) =
        GLES30.glTexStorage2D(target, levels.toInt(), internalformat, width.toInt(), height.toInt())

    override fun glTexStorage3D(
        target: Int, levels: UInt, internalformat: Int, width: UInt, height: UInt, depth: UInt
    ) = GLES30.glTexStorage3D(target, levels.toInt(), internalformat, width.toInt(), height.toInt(), depth.toInt())

    override fun glTexSubImage3D(
        target: Int,
        level: Int,
        xoffset: Int,
        yoffset: Int,
        zoffset: Int,
        width: UInt,
        height: UInt,
        depth: UInt,
        format: Int,
        type: Int,
        data: BufferRO
    ) = GLES30.nglTexSubImage3D(
        target,
        level,
        xoffset,
        yoffset,
        zoffset,
        width.toInt(),
        height.toInt(),
        depth.toInt(),
        format,
        type,
        data.address
    )

    override fun glTransformFeedbackVaryings(program: UInt, count: UInt, varyings: PointerBuffer, bufferMode: Int) =
        GLES30.nglTransformFeedbackVaryings(program.toInt(), count.toInt(), varyings.address, bufferMode)

    override fun glUniform1ui(location: Int, v0: UInt) = GLES30.glUniform1ui(location, v0.toInt())

    override fun glUniform1uiv(location: Int, count: UInt, value: IntBufferRO) =
        GLES30.nglUniform1uiv(location, count.toInt(), value.address)

    override fun glUniform2ui(location: Int, v0: UInt, v1: UInt) = GLES30.glUniform2ui(location, v0.toInt(), v1.toInt())

    override fun glUniform2uiv(location: Int, count: UInt, value: IntBufferRO) =
        GLES30.nglUniform2uiv(location, count.toInt(), value.address)

    override fun glUniform3ui(location: Int, v0: UInt, v1: UInt, v2: UInt) =
        GLES30.glUniform3ui(location, v0.toInt(), v1.toInt(), v2.toInt())

    override fun glUniform3uiv(location: Int, count: UInt, value: IntBufferRO) =
        GLES30.nglUniform3uiv(location, count.toInt(), value.address)

    override fun glUniform4ui(location: Int, v0: Int, v1: UInt, v2: UInt, v3: UInt) =
        GLES30.glUniform4ui(location, v0, v1.toInt(), v2.toInt(), v3.toInt())

    override fun glUniform4uiv(location: Int, count: UInt, value: IntBufferRO) =
        GLES30.nglUniform4uiv(location, count.toInt(), value.address)

    override fun glUniformBlockBinding(program: UInt, uniformBlockIndex: UInt, uniformBlockBinding: UInt) =
        GLES30.glUniformBlockBinding(program.toInt(), uniformBlockIndex.toInt(), uniformBlockBinding.toInt())

    override fun glUniformMatrix2x3fv(location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO) =
        GLES30.nglUniformMatrix2x3fv(location, count.toInt(), transpose, value.address)

    override fun glUniformMatrix2x4fv(location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO) =
        GLES30.nglUniformMatrix2x4fv(location, count.toInt(), transpose, value.address)

    override fun glUniformMatrix3x2fv(location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO) =
        GLES30.nglUniformMatrix3x2fv(location, count.toInt(), transpose, value.address)

    override fun glUniformMatrix3x4fv(location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO) =
        GLES30.nglUniformMatrix3x4fv(location, count.toInt(), transpose, value.address)

    override fun glUniformMatrix4x2fv(location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO) =
        GLES30.nglUniformMatrix4x2fv(location, count.toInt(), transpose, value.address)

    override fun glUniformMatrix4x3fv(location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO) =
        GLES30.nglUniformMatrix4x3fv(location, count.toInt(), transpose, value.address)

    override fun glUnmapBuffer(target: Int): Boolean = GLES30.glUnmapBuffer(target)

    override fun glVertexAttribDivisor(index: UInt, divisor: UInt) =
        GLES30.glVertexAttribDivisor(index.toInt(), divisor.toInt())

    override fun glVertexAttribI4i(index: UInt, v0: Int, v1: Int, v2: Int, v3: Int) =
        GLES30.glVertexAttribI4i(index.toInt(), v0, v1, v2, v3)

    override fun glVertexAttribI4iv(index: UInt, v: IntBufferRO) = GLES30.nglVertexAttribI4iv(index.toInt(), v.address)

    override fun glVertexAttribI4ui(index: UInt, v0: UInt, v1: UInt, v2: UInt, v3: UInt) =
        GLES30.glVertexAttribI4ui(index.toInt(), v0.toInt(), v1.toInt(), v2.toInt(), v3.toInt())

    override fun glVertexAttribI4uiv(index: UInt, v: IntBufferRO) =
        GLES30.nglVertexAttribI4uiv(index.toInt(), v.address)

    override fun glVertexAttribIPointer(index: UInt, size: Int, type: Int, stride: UInt, pointer: BufferRO) =
        GLES30.nglVertexAttribIPointer(index.toInt(), size, type, stride.toInt(), pointer.address)

    override fun glWaitSync(sync: Long, flags: Int, timeout: ULong) = GLES30.nglWaitSync(sync, flags, timeout.toLong())
}