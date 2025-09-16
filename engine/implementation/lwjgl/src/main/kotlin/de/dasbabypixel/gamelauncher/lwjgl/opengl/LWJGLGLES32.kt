package de.dasbabypixel.gamelauncher.lwjgl.opengl

import de.dasbabypixel.gamelauncher.buffers.*
import de.dasbabypixel.gamelauncher.gles.es32.DebugProc
import org.lwjgl.opengles.GLDebugMessageCallback
import org.lwjgl.opengles.GLES32

abstract class LWJGLGLES32 : LWJGLGLES31(), de.dasbabypixel.gamelauncher.gles.GLES32 {
    override fun glBlendBarrier() = GLES32.glBlendBarrier()

    override fun glBlendEquationSeparatei(buf: UInt, modeRGB: Int, modeAlpha: Int) =
        GLES32.glBlendEquationSeparatei(buf.toInt(), modeRGB, modeAlpha)

    override fun glBlendEquationi(buf: UInt, mode: Int) = GLES32.glBlendEquationi(buf.toInt(), mode)

    override fun glBlendFuncSeparatei(
        buf: UInt, srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int
    ) = GLES32.glBlendFuncSeparatei(buf.toInt(), srcRGB, dstRGB, srcAlpha, dstAlpha)

    override fun glBlendFunci(buf: UInt, sfactor: Int, dfactor: Int) =
        GLES32.glBlendFunci(buf.toInt(), sfactor, dfactor)

    override fun glColorMaski(
        buf: UInt, red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean
    ) = GLES32.glColorMaski(buf.toInt(), red, green, blue, alpha)

    override fun glCopyImageSubData(
        srcName: UInt,
        srcTarget: Int,
        srcLevel: Int,
        srcX: Int,
        srcY: Int,
        srcZ: Int,
        dstName: UInt,
        dstTarget: Int,
        dstLevel: Int,
        dstX: Int,
        dstY: Int,
        dstZ: Int,
        srcWidth: UInt,
        srcHeight: UInt,
        srcDepth: UInt
    ) = GLES32.glCopyImageSubData(
        srcName.toInt(),
        srcTarget,
        srcLevel,
        srcX,
        srcY,
        srcZ,
        dstName.toInt(),
        dstTarget,
        dstLevel,
        dstX,
        dstY,
        dstZ,
        srcWidth.toInt(),
        srcHeight.toInt(),
        srcDepth.toInt()
    )

    override fun glDebugMessageCallback(
        callback: DebugProc, userParam: BufferRO
    ) = GLES32.glDebugMessageCallback({ source, type, id, severity, length, message, _ ->
        {
            val msg = GLDebugMessageCallback.getMessage(length, message)
            callback.DebugProc(type, id.toUInt(), severity, length.toUInt(), msg, userParam)
        }
    }, 0L)

    override fun glDebugMessageControl(
        source: Int, type: Int, severity: Int, count: UInt, ids: IntBufferRO, enabled: Boolean
    ) = GLES32.nglDebugMessageControl(source, type, severity, count.toInt(), ids.address, enabled)

    override fun glDebugMessageInsert(
        source: Int, type: Int, id: UInt, severity: Int, length: UInt, message: String?
    ) = GLES32.glDebugMessageInsert(source, type, id.toInt(), severity, message!!)

    override fun glDisablei(cap: Int, index: UInt) = GLES32.glDisablei(cap, index.toInt())

    override fun glDrawElementsBaseVertex(
        mode: Int, count: UInt, type: Int, indices: Buffer, basevertex: Int
    ) = GLES32.nglDrawElementsBaseVertex(mode, count.toInt(), type, indices.address, basevertex)

    override fun glDrawElementsInstancedBaseVertex(
        mode: Int, count: UInt, type: Int, indices: Buffer, primcount: UInt, basevertex: Int
    ) = GLES32.nglDrawElementsInstancedBaseVertex(
        mode, count.toInt(), type, indices.address, primcount.toInt(), basevertex
    )

    override fun glDrawRangeElementsBaseVertex(
        mode: Int, start: UInt, end: UInt, count: UInt, type: Int, indices: Buffer, basevertex: Int
    ) = GLES32.nglDrawRangeElementsBaseVertex(
        mode, start.toInt(), end.toInt(), count.toInt(), type, indices.address, basevertex
    )

    override fun glEnablei(cap: Int, index: UInt) = GLES32.glEnablei(cap, index.toInt())

    override fun glFramebufferTexture(
        target: Int, attachment: Int, texture: UInt, level: Int
    ) = GLES32.glFramebufferTexture(target, attachment, texture.toInt(), level)

    override fun glGetDebugMessageLog(
        count: UInt,
        bufSize: UInt,
        sources: IntBuffer,
        types: IntBuffer,
        ids: IntBuffer,
        severities: IntBuffer,
        lengths: IntBuffer,
        messageLog: ByteBuffer
    ): UInt = GLES32.nglGetDebugMessageLog(
        count.toInt(),
        bufSize.toInt(),
        sources.address,
        types.address,
        ids.address,
        severities.address,
        lengths.address,
        messageLog.address
    ).toUInt()

    override fun glGetGraphicsResetStatus(): Int = GLES32.glGetGraphicsResetStatus()

    override fun glGetObjectLabel(
        identifier: Int, name: UInt, bifSize: UInt, length: IntBuffer, label: ByteBuffer
    ) = GLES32.nglGetObjectLabel(identifier, name.toInt(), bifSize.toInt(), length.address, label.address)

    override fun glGetObjectPtrLabel(
        ptr: Buffer, bifSize: UInt, length: IntBuffer, label: ByteBuffer
    ) = GLES32.nglGetObjectPtrLabel(ptr.address, bifSize.toInt(), length.address, label.address)

    override fun glGetPointerv(pname: Int, params: PointerBuffer) = GLES32.nglGetPointerv(pname, params.address)

    override fun glGetSamplerParameterIiv(
        sampler: UInt, pname: Int, params: IntBuffer
    ) = GLES32.nglGetSamplerParameterIiv(sampler.toInt(), pname, params.address)

    override fun glGetSamplerParameterIuiv(
        sampler: UInt, pname: Int, params: IntBuffer
    ) = GLES32.nglGetSamplerParameterIuiv(sampler.toInt(), pname, params.address)

    override fun glGetTexParameterIiv(
        target: Int, pname: Int, params: IntBuffer
    ) = GLES32.nglGetTexParameterIiv(target, pname, params.address)

    override fun glGetTexParameterIuiv(
        target: Int, pname: Int, params: IntBuffer
    ) = GLES32.nglGetTexParameterIuiv(target, pname, params.address)

    override fun glGetnUniformfv(
        program: UInt, location: Int, bufSize: UInt, params: FloatBuffer
    ) = GLES32.nglGetnUniformfv(program.toInt(), location, bufSize.toInt(), params.address)

    override fun glGetnUniformiv(
        program: UInt, location: Int, bufSize: UInt, params: IntBuffer
    ) = GLES32.nglGetnUniformiv(program.toInt(), location, bufSize.toInt(), params.address)

    override fun glGetnUniformuiv(
        program: UInt, location: Int, bufSize: UInt, params: IntBuffer
    ) = GLES32.nglGetnUniformuiv(program.toInt(), location, bufSize.toInt(), params.address)

    override fun glIsEnabledi(cap: Int, index: UInt): Boolean = GLES32.glIsEnabledi(cap, index.toInt())

    override fun glMinSampleShading(value: Float) = GLES32.glMinSampleShading(value)

    override fun glObjectLabel(identifier: Int, name: UInt, length: UInt, label: String?) =
        GLES32.glObjectLabel(identifier, name.toInt(), label!!)

    override fun glObjectPtrLabel(
        ptr: Buffer, length: UInt, label: String?
    ) = GLES32.glObjectPtrLabel(ptr.address, label!!)

    override fun glPatchParameteri(pname: Int, value: Int) = GLES32.glPatchParameteri(pname, value)

    override fun glPopDebugGroup() = GLES32.glPopDebugGroup()

    override fun glPrimitiveBoundingBox(
        minX: Float, minY: Float, minZ: Float, minW: Float, maxX: Float, maxY: Float, maxZ: Float, maxW: Float
    ) = GLES32.glPrimitiveBoundingBox(minX, minY, minZ, minW, maxX, maxY, maxZ, maxW)

    override fun glPushDebugGroup(source: Int, id: UInt, length: UInt, message: String?) =
        GLES32.glPushDebugGroup(source, id.toInt(), message!!)

    override fun glReadnPixels(
        x: Int, y: Int, width: UInt, height: UInt, format: Int, type: Int, bufSize: UInt, data: Buffer
    ) = GLES32.nglReadnPixels(x, y, width.toInt(), height.toInt(), format, type, bufSize.toInt(), data.address)

    override fun glSamplerParameterIiv(
        sampler: UInt, pname: Int, params: IntBufferRO
    ) = GLES32.nglSamplerParameterIiv(sampler.toInt(), pname, params.address)

    override fun glSamplerParameterIuiv(
        sampler: UInt, pname: Int, params: IntBufferRO
    ) = GLES32.nglSamplerParameterIuiv(sampler.toInt(), pname, params.address)

    override fun glTexBuffer(target: Int, internalFormat: Int, buffer: UInt) =
        GLES32.glTexBuffer(target, internalFormat, buffer.toInt())

    override fun glTexBufferRange(
        target: Int, internalFormat: Int, buffer: UInt, offset: Long, size: ULong
    ) = GLES32.glTexBufferRange(target, internalFormat, buffer.toInt(), offset, size.toLong())

    override fun glTexParameterIiv(
        target: Int, pname: Int, params: IntBufferRO
    ) = GLES32.nglTexParameterIiv(target, pname, params.address)

    override fun glTexParameterIuiv(
        target: Int, pname: Int, params: IntBufferRO
    ) = GLES32.nglTexParameterIuiv(target, pname, params.address)

    override fun glTexStorage3DMultisample(
        target: Int,
        samples: UInt,
        internalformat: Int,
        width: UInt,
        height: UInt,
        depth: UInt,
        fixedsamplelocations: Boolean
    ) = GLES32.glTexStorage3DMultisample(
        target, samples.toInt(), internalformat, width.toInt(), height.toInt(), depth.toInt(), fixedsamplelocations
    )
}