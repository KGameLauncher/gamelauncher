package de.dasbabypixel.gamelauncher.lwjgl.opengl

import de.dasbabypixel.gamelauncher.buffers.*
import de.dasbabypixel.gamelauncher.lwjgl.buffers.LWJGLPointer
import org.lwjgl.opengles.GLES20

abstract class LWJGLGLES20 : de.dasbabypixel.gamelauncher.gles.GLES20 {
    override fun glActiveTexture(texture: Int) = GLES20.glActiveTexture(texture)

    override fun glAttachShader(program: UInt, shader: UInt) = GLES20.glAttachShader(program.toInt(), shader.toInt())

    override fun glBindAttribLocation(program: UInt, index: UInt, name: String?) =
        GLES20.glBindAttribLocation(program.toInt(), index.toInt(), name!!)

    override fun glBindBuffer(target: Int, buffer: UInt) = GLES20.glBindBuffer(target, buffer.toInt())

    override fun glBindFramebuffer(target: Int, framebuffer: UInt) =
        GLES20.glBindFramebuffer(target, framebuffer.toInt())

    override fun glBindRenderbuffer(target: Int, renderbuffer: UInt) =
        GLES20.glBindRenderbuffer(target, renderbuffer.toInt())

    override fun glBindTexture(target: Int, texture: UInt) = GLES20.glBindTexture(target, texture.toInt())

    override fun glBlendColor(red: Float, green: Float, blue: Float, alpha: Float) =
        GLES20.glBlendColor(red, green, blue, alpha)

    override fun glBlendEquation(mode: Int) = GLES20.glBlendEquation(mode)

    override fun glBlendEquationSeparate(modeRGB: Int, modeAlpha: Int) =
        GLES20.glBlendEquationSeparate(modeRGB, modeAlpha)

    override fun glBlendFunc(sfactor: Int, dfactor: Int) = GLES20.glBlendFunc(sfactor, dfactor)

    override fun glBlendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int) =
        GLES20.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha)

    override fun glBufferData(target: Int, size: ULong, data: BufferRO, usage: Int) =
        GLES20.nglBufferData(target, size.toLong(), data.address, usage)

    override fun glBufferSubData(target: Int, offset: Long, size: ULong, data: BufferRO) =
        GLES20.nglBufferSubData(target, offset, size.toLong(), data.address)

    override fun glCheckFramebufferStatus(target: Int): Int = GLES20.glCheckFramebufferStatus(target)

    override fun glClear(mask: Int) = GLES20.glClear(mask)

    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) =
        GLES20.glClearColor(red, green, blue, alpha)

    override fun glClearDepthf(depth: Float) = GLES20.glClearDepthf(depth)

    override fun glClearStencil(s: Int) = GLES20.glClearStencil(s)

    override fun glColorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean) =
        GLES20.glColorMask(red, green, blue, alpha)

    override fun glCompileShader(shader: UInt) = GLES20.glCompileShader(shader.toInt())

    override fun glCompressedTexImage2D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: UInt,
        height: UInt,
        border: Int,
        imageSize: UInt,
        data: BufferRO
    ) = GLES20.glCompressedTexImage2D(
        target, level, internalformat, width.toInt(), height.toInt(), border, imageSize.toInt(), data.address
    )

    override fun glCompressedTexSubImage2D(
        target: Int,
        level: Int,
        xoffset: Int,
        yoffset: Int,
        width: UInt,
        height: UInt,
        format: Int,
        imageSize: UInt,
        data: BufferRO
    ) = GLES20.glCompressedTexSubImage2D(
        target, level, xoffset, yoffset, width.toInt(), height.toInt(), format, imageSize.toInt(), data.address
    )

    override fun glCopyTexImage2D(
        target: Int, level: Int, internalformat: Int, x: Int, y: Int, width: UInt, height: UInt, border: Int
    ) = GLES20.glCopyTexImage2D(target, level, internalformat, x, y, width.toInt(), height.toInt(), border)

    override fun glCopyTexSubImage2D(
        target: Int, level: Int, xoffset: Int, yoffset: Int, x: Int, y: Int, width: UInt, height: UInt
    ) = GLES20.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width.toInt(), height.toInt())

    override fun glCreateProgram(): UInt = GLES20.glCreateProgram().toUInt()

    override fun glCreateShader(shaderType: Int): UInt = GLES20.glCreateShader(shaderType).toUInt()

    override fun glCullFace(mode: Int) = GLES20.glCullFace(mode)

    override fun glDeleteBuffers(n: UInt, buffers: IntBufferRO) = GLES20.nglDeleteBuffers(n.toInt(), buffers.address)

    override fun glDeleteFramebuffers(n: UInt, framebuffers: IntBuffer) =
        GLES20.nglDeleteFramebuffers(n.toInt(), (framebuffers as LWJGLPointer).address)

    override fun glDeleteProgram(program: UInt) = GLES20.glDeleteProgram(program.toInt())

    override fun glDeleteRenderbuffers(n: UInt, renderbuffers: IntBuffer) =
        GLES20.nglDeleteRenderbuffers(n.toInt(), renderbuffers.address)

    override fun glDeleteShader(shader: UInt) = GLES20.glDeleteShader(shader.toInt())

    override fun glDeleteTextures(n: UInt, textures: IntBufferRO) =
        GLES20.nglDeleteTextures(n.toInt(), textures.address)

    override fun glDepthFunc(func: Int) = GLES20.glDepthFunc(func)

    override fun glDepthMask(flag: Boolean) = GLES20.glDepthMask(flag)

    override fun glDepthRangef(n: Float, f: Float) = GLES20.glDepthRangef(n, f)

    override fun glDetachShader(program: UInt, shader: UInt) = GLES20.glDetachShader(program.toInt(), shader.toInt())

    override fun glDisable(cap: Int) = GLES20.glDisable(cap)

    override fun glDisableVertexAttribArray(index: UInt) = GLES20.glDisableVertexAttribArray(index.toInt())

    override fun glDrawArrays(mode: Int, first: Int, count: UInt) = GLES20.glDrawArrays(mode, first, count.toInt())

    override fun glDrawElements(mode: Int, count: UInt, type: Int, indices: BufferRO) =
        GLES20.glDrawElements(mode, count.toInt(), type, indices.address)

    override fun glEnable(cap: Int) = GLES20.glEnable(cap)

    override fun glEnableVertexAttribArray(index: UInt) = GLES20.glEnableVertexAttribArray(index.toInt())

    override fun glFinish() = GLES20.glFinish()

    override fun glFlush() = GLES20.glFlush()

    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: UInt) =
        GLES20.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer.toInt())

    override fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: UInt, level: Int) =
        GLES20.glFramebufferTexture2D(target, attachment, textarget, texture.toInt(), level)

    override fun glFrontFace(mode: Int) = GLES20.glFrontFace(mode)

    override fun glGenBuffers(n: UInt, buffers: IntBuffer) = GLES20.nglGenBuffers(n.toInt(), buffers.address)

    override fun glGenFramebuffers(n: UInt, framebuffers: IntBuffer) =
        GLES20.nglGenFramebuffers(n.toInt(), framebuffers.address)

    override fun glGenRenderbuffers(n: UInt, renderbuffers: IntBuffer) =
        GLES20.nglGenRenderbuffers(n.toInt(), renderbuffers.address)

    override fun glGenTextures(n: UInt, textures: IntBuffer) = GLES20.nglGenTextures(n.toInt(), textures.address)

    override fun glGenerateMipmap(target: Int) = GLES20.glGenerateMipmap(target)

    override fun glGetActiveAttrib(
        program: UInt, index: UInt, bufSize: UInt, length: IntBuffer, size: IntBuffer, type: IntBuffer, name: ByteBuffer
    ) = GLES20.nglGetActiveAttrib(
        program.toInt(), index.toInt(), bufSize.toInt(), length.address, size.address, type.address, name.address
    )

    override fun glGetActiveUniform(
        program: UInt, index: UInt, bufSize: UInt, length: IntBuffer, size: IntBuffer, type: IntBuffer, name: ByteBuffer
    ) = GLES20.nglGetActiveUniform(
        program.toInt(), index.toInt(), bufSize.toInt(), length.address, size.address, type.address, name.address
    )

    override fun glGetAttachedShaders(program: UInt, maxCount: UInt, count: IntBuffer, shaders: IntBuffer) =
        GLES20.nglGetAttachedShaders(program.toInt(), maxCount.toInt(), count.address, shaders.address)

    override fun glGetAttribLocation(program: UInt, name: String?): Int =
        GLES20.glGetAttribLocation(program.toInt(), name!!)

    override fun glGetBooleanv(pname: Int, data: ByteBuffer) = GLES20.nglGetBooleanv(pname, data.address)

    override fun glGetBufferParameteriv(target: Int, value: Int, data: IntBuffer) =
        GLES20.nglGetBufferParameteriv(target, value, data.address)

    override fun glGetError(): Int = GLES20.glGetError()

    override fun glGetFloatv(pname: Int, data: FloatBuffer) = GLES20.nglGetFloatv(pname, data.address)

    override fun glGetFramebufferAttachmentParameteriv(target: Int, attachment: Int, pname: Int, params: IntBuffer) =
        GLES20.nglGetFramebufferAttachmentParameteriv(target, attachment, pname, params.address)

    override fun glGetIntegerv(pname: Int, data: IntBuffer) = GLES20.nglGetIntegerv(pname, data.address)

    override fun glGetProgramInfoLog(program: UInt, maxLength: UInt, length: IntBuffer, infoLog: ByteBuffer) =
        GLES20.nglGetProgramInfoLog(program.toInt(), maxLength.toInt(), length.address, infoLog.address)

    override fun glGetProgramiv(program: UInt, pname: Int, params: IntBuffer) =
        GLES20.nglGetProgramiv(program.toInt(), pname, params.address)

    override fun glGetRenderbufferParameteriv(target: Int, pname: Int, params: IntBuffer) =
        GLES20.nglGetRenderbufferParameteriv(target, pname, params.address)

    override fun glGetShaderInfoLog(shader: UInt, maxLength: UInt, length: IntBuffer, infoLog: ByteBuffer) =
        GLES20.nglGetShaderInfoLog(shader.toInt(), maxLength.toInt(), length.address, infoLog.address)

    override fun glGetShaderPrecisionFormat(
        shaderType: Int, precisionType: Int, range: IntBuffer, precision: IntBuffer
    ) = GLES20.nglGetShaderPrecisionFormat(shaderType, precisionType, range.address, precision.address)

    override fun glGetShaderSource(shader: UInt, bufSize: UInt, length: IntBuffer, source: ByteBuffer) =
        GLES20.nglGetShaderSource(shader.toInt(), bufSize.toInt(), length.address, source.address)

    override fun glGetShaderiv(shader: UInt, pname: Int, params: IntBuffer) =
        GLES20.nglGetShaderiv(shader.toInt(), pname, params.address)

    override fun glGetString(name: Int): String? = GLES20.glGetString(name)

    override fun glGetTexParameterfv(target: Int, pname: Int, params: FloatBuffer) =
        GLES20.nglGetTexParameterfv(target, pname, params.address)

    override fun glGetTexParameteriv(target: Int, pname: Int, params: IntBuffer) =
        GLES20.nglGetTexParameteriv(target, pname, params.address)

    override fun glGetUniformLocation(program: UInt, name: String?): Int =
        GLES20.glGetUniformLocation(program.toInt(), name!!)


    override fun glGetUniformfv(program: UInt, location: Int, params: FloatBuffer) =
        GLES20.nglGetUniformfv(program.toInt(), location, params.address)

    override fun glGetUniformiv(program: UInt, location: Int, params: IntBuffer) =
        GLES20.nglGetUniformiv(program.toInt(), location, params.address)

    override fun glGetVertexAttribPointerv(index: UInt, pname: Int, pointer: PointerBuffer) =
        GLES20.nglGetVertexAttribPointerv(index.toInt(), pname, pointer.address)

    override fun glGetVertexAttribfv(index: UInt, pname: Int, params: FloatBuffer) =
        GLES20.nglGetVertexAttribfv(index.toInt(), pname, params.address)

    override fun glGetVertexAttribiv(index: UInt, pname: Int, params: IntBuffer) =
        GLES20.nglGetVertexAttribiv(index.toInt(), pname, params.address)

    override fun glHint(target: Int, mode: Int) = GLES20.glHint(target, mode)

    override fun glIsBuffer(buffer: UInt): Boolean = GLES20.glIsBuffer(buffer.toInt())

    override fun glIsEnabled(cap: Int): Boolean = GLES20.glIsEnabled(cap)

    override fun glIsFramebuffer(framebuffer: UInt): Boolean = GLES20.glIsFramebuffer(framebuffer.toInt())

    override fun glIsProgram(program: UInt): Boolean = GLES20.glIsProgram(program.toInt())

    override fun glIsRenderbuffer(renderbuffer: UInt): Boolean = GLES20.glIsRenderbuffer(renderbuffer.toInt())

    override fun glIsShader(shader: UInt): Boolean = GLES20.glIsShader(shader.toInt())

    override fun glIsTexture(texture: UInt): Boolean = GLES20.glIsTexture(texture.toInt())

    override fun glLineWidth(width: Float) = GLES20.glLineWidth(width)

    override fun glLinkProgram(program: UInt) = GLES20.glLinkProgram(program.toInt())

    override fun glPixelStorei(pname: Int, param: Int) = GLES20.glPixelStorei(pname, param)

    override fun glPolygonOffset(factor: Float, units: Float) = GLES20.glPolygonOffset(factor, units)

    override fun glReadPixels(x: Int, y: Int, width: UInt, height: UInt, format: Int, type: Int, data: Buffer) =
        GLES20.nglReadPixels(x, y, width.toInt(), height.toInt(), format, type, data.address)

    override fun glReleaseShaderCompiler() = GLES20.glReleaseShaderCompiler()

    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: UInt, height: UInt) =
        GLES20.glRenderbufferStorage(target, internalformat, width.toInt(), height.toInt())

    override fun glSampleCoverage(value: Float, invert: Boolean) = GLES20.glSampleCoverage(value, invert)

    override fun glScissor(x: Int, y: Int, width: UInt, height: UInt) =
        GLES20.glScissor(x, y, width.toInt(), height.toInt())

    override fun glShaderBinary(count: UInt, shaders: IntBufferRO, binaryFormat: Int, binary: BufferRO, length: UInt) =
        GLES20.nglShaderBinary(count.toInt(), shaders.address, binaryFormat, binary.address, length.toInt())

    override fun glShaderSource(shader: UInt, count: UInt, string: PointerBuffer, length: IntBufferRO) =
        GLES20.nglShaderSource(shader.toInt(), count.toInt(), string.address, length.address)

    override fun glStencilFunc(func: Int, ref: Int, mask: UInt) = GLES20.glStencilFunc(func, ref, mask.toInt())

    override fun glStencilFuncSeparate(face: Int, func: Int, ref: Int, mask: UInt) =
        GLES20.glStencilFuncSeparate(face, func, ref, mask.toInt())

    override fun glStencilMask(mask: UInt) = GLES20.glStencilMask(mask.toInt())

    override fun glStencilMaskSeparate(face: Int, mask: UInt) = GLES20.glStencilMaskSeparate(face, mask.toInt())

    override fun glStencilOp(sfail: Int, dpfail: Int, dppass: Int) = GLES20.glStencilOp(sfail, dpfail, dppass)

    override fun glStencilOpSeparate(face: Int, sfail: Int, dpfail: Int, dppass: Int) =
        GLES20.glStencilOpSeparate(face, sfail, dpfail, dppass)

    override fun glTexImage2D(
        target: Int,
        level: Int,
        internalFormat: Int,
        width: UInt,
        height: UInt,
        border: Int,
        format: Int,
        type: Int,
        data: BufferRO
    ) = GLES20.nglTexImage2D(
        target, level, internalFormat, width.toInt(), height.toInt(), border, format, type, data.address
    )

    override fun glTexParameterf(target: Int, pname: Int, param: Float) = GLES20.glTexParameterf(target, pname, param)

    override fun glTexParameterfv(target: Int, pname: Int, params: FloatBufferRO) =
        GLES20.nglTexParameterfv(target, pname, params.address)

    override fun glTexParameteri(target: Int, pname: Int, param: Int) = GLES20.glTexParameteri(target, pname, param)

    override fun glTexParameteriv(target: Int, pname: Int, params: IntBufferRO) =
        GLES20.nglTexParameteriv(target, pname, params.address)

    override fun glTexSubImage2D(
        target: Int,
        level: Int,
        xoffset: Int,
        yoffset: Int,
        width: UInt,
        height: UInt,
        format: Int,
        type: Int,
        data: BufferRO
    ) = GLES20.nglTexSubImage2D(
        target, level, xoffset, yoffset, width.toInt(), height.toInt(), format, type, data.address
    )

    override fun glUniform1f(location: Int, v0: Float) = GLES20.glUniform1f(location, v0)

    override fun glUniform1fv(location: Int, count: UInt, value: FloatBufferRO) =
        GLES20.nglUniform1fv(location, count.toInt(), value.address)

    override fun glUniform1i(location: Int, v0: Int) = GLES20.glUniform1i(location, v0)

    override fun glUniform1iv(location: Int, count: UInt, value: IntBufferRO) =
        GLES20.nglUniform1iv(location, count.toInt(), value.address)

    override fun glUniform2f(location: Int, v0: Float, v1: Float) = GLES20.glUniform2f(location, v0, v1)

    override fun glUniform2fv(location: Int, count: UInt, value: FloatBufferRO) =
        GLES20.nglUniform2fv(location, count.toInt(), value.address)

    override fun glUniform2i(location: Int, v0: Int, v1: Int) = GLES20.glUniform2i(location, v0, v1)

    override fun glUniform2iv(location: Int, count: UInt, value: IntBufferRO) =
        GLES20.nglUniform2iv(location, count.toInt(), value.address)

    override fun glUniform3f(location: Int, v0: Float, v1: Float, v2: Float) = GLES20.glUniform3f(location, v0, v1, v2)

    override fun glUniform3fv(location: Int, count: UInt, value: FloatBufferRO) =
        GLES20.nglUniform3fv(location, count.toInt(), value.address)

    override fun glUniform3i(location: Int, v0: Int, v1: Int, v2: Int) = GLES20.glUniform3i(location, v0, v1, v2)

    override fun glUniform3iv(location: Int, count: UInt, value: IntBufferRO) =
        GLES20.nglUniform3iv(location, count.toInt(), value.address)

    override fun glUniform4f(location: Int, v0: Float, v1: Float, v2: Float, v3: Float) =
        GLES20.glUniform4f(location, v0, v1, v2, v3)

    override fun glUniform4fv(location: Int, count: UInt, value: FloatBufferRO) =
        GLES20.nglUniform4fv(location, count.toInt(), value.address)

    override fun glUniform4i(location: Int, v0: Int, v1: Int, v2: Int, v3: Int) =
        GLES20.glUniform4i(location, v0, v1, v2, v3)

    override fun glUniform4iv(location: Int, count: UInt, value: IntBufferRO) =
        GLES20.nglUniform4iv(location, count.toInt(), value.address)

    override fun glUniformMatrix2fv(location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO) =
        GLES20.nglUniformMatrix2fv(location, count.toInt(), transpose, value.address)

    override fun glUniformMatrix3fv(location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO) =
        GLES20.nglUniformMatrix3fv(location, count.toInt(), transpose, value.address)

    override fun glUniformMatrix4fv(location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO) =
        GLES20.nglUniformMatrix4fv(location, count.toInt(), transpose, value.address)

    override fun glUseProgram(program: UInt) = GLES20.glUseProgram(program.toInt())

    override fun glValidateProgram(program: UInt) = GLES20.glValidateProgram(program.toInt())

    override fun glVertexAttrib1f(index: UInt, v0: Float) = GLES20.glVertexAttrib1f(index.toInt(), v0)

    override fun glVertexAttrib1fv(index: UInt, v: FloatBufferRO) = GLES20.nglVertexAttrib1fv(index.toInt(), v.address)

    override fun glVertexAttrib2f(index: UInt, v0: Float, v1: Float) = GLES20.glVertexAttrib2f(index.toInt(), v0, v1)

    override fun glVertexAttrib2fv(index: UInt, v: FloatBufferRO) = GLES20.nglVertexAttrib2fv(index.toInt(), v.address)

    override fun glVertexAttrib3f(index: UInt, v0: Float, v1: Float, v2: Float) =
        GLES20.glVertexAttrib3f(index.toInt(), v0, v1, v2)

    override fun glVertexAttrib3fv(index: UInt, v: FloatBufferRO) = GLES20.nglVertexAttrib3fv(index.toInt(), v.address)

    override fun glVertexAttrib4f(index: UInt, v0: Float, v1: Float, v2: Float, v3: Float) =
        GLES20.glVertexAttrib4f(index.toInt(), v0, v1, v2, v3)

    override fun glVertexAttrib4fv(index: UInt, v: FloatBufferRO) = GLES20.nglVertexAttrib4fv(index.toInt(), v.address)

    override fun glVertexAttribPointer(
        index: UInt, size: Int, type: Int, normalized: Boolean, stride: UInt, pointer: BufferRO
    ) = GLES20.nglVertexAttribPointer(index.toInt(), size, type, normalized, stride.toInt(), pointer.address)

    override fun glViewport(x: Int, y: Int, width: UInt, height: UInt) =
        GLES20.glViewport(x, y, width.toInt(), height.toInt())

    protected val Pointer.address
        get() = (this as LWJGLPointer).address
}