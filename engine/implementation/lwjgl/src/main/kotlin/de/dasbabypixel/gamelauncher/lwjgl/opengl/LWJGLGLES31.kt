package de.dasbabypixel.gamelauncher.lwjgl.opengl

import de.dasbabypixel.gamelauncher.buffers.*
import org.lwjgl.opengles.GLES31

abstract class LWJGLGLES31 : LWJGLGLES30(), de.dasbabypixel.gamelauncher.gles.GLES31 {
    override fun glActiveShaderProgram(pipeline: UInt, program: UInt) =
        GLES31.glActiveShaderProgram(pipeline.toInt(), program.toInt())

    override fun glBindImageTexture(
        unit: UInt, texture: UInt, level: Int, layered: Boolean, layer: Int, access: Int, format: Int
    ) = GLES31.glBindImageTexture(unit.toInt(), texture.toInt(), level, layered, layer, access, format)

    override fun glBindProgramPipeline(pipeline: UInt) = GLES31.glBindProgramPipeline(pipeline.toInt())

    override fun glBindVertexBuffer(
        bindingindex: UInt, buffer: UInt, offset: Long, stride: Long
    ) = GLES31.glBindVertexBuffer(bindingindex.toInt(), buffer.toInt(), offset, stride.toInt())

    override fun glCreateShaderProgramv(
        type: Int, count: UInt, strings: PointerBuffer
    ): UInt = GLES31.nglCreateShaderProgramv(type, count.toInt(), strings.address).toUInt()

    override fun glDeleteProgramPipelines(n: UInt, pipelines: IntBufferRO) =
        GLES31.nglDeleteProgramPipelines(n.toInt(), pipelines.address)

    override fun glDispatchCompute(num_groups_x: UInt, num_groups_y: UInt, num_groups_z: UInt) =
        GLES31.glDispatchCompute(num_groups_x.toInt(), num_groups_y.toInt(), num_groups_z.toInt())

    override fun glDispatchComputeIndirect(indirect: Long) = GLES31.glDispatchComputeIndirect(indirect)

    override fun glDrawArraysIndirect(mode: Int, indirect: BufferRO) =
        GLES31.nglDrawArraysIndirect(mode, indirect.address)

    override fun glDrawElementsIndirect(
        mode: Int, type: Int, indirect: BufferRO
    ) = GLES31.nglDrawElementsIndirect(mode, type, indirect.address)

    override fun glFramebufferParameteri(target: Int, pname: Int, param: Int) =
        GLES31.glFramebufferParameteri(target, pname, param)

    override fun glGenProgramPipelines(n: UInt, pipelines: IntBuffer) =
        GLES31.nglGenProgramPipelines(n.toInt(), pipelines.address)

    override fun glGetBooleani_v(
        target: Int, index: UInt, data: ByteBuffer
    ) = GLES31.nglGetBooleani_v(target, index.toInt(), data.address)

    override fun glGetFramebufferParameteriv(
        target: Int, pname: Int, params: IntBuffer
    ) = GLES31.nglGetFramebufferParameteriv(target, pname, params.address)

    override fun glGetMultisamplefv(
        pname: Int, index: UInt, `val`: FloatBuffer
    ) = GLES31.nglGetMultisamplefv(pname, index.toInt(), `val`.address)

    override fun glGetProgramInterfaceiv(
        program: UInt, programInterface: Int, pname: Int, params: IntBuffer
    ) = GLES31.nglGetProgramInterfaceiv(program.toInt(), programInterface, pname, params.address)

    override fun glGetProgramPipelineInfoLog(
        pipeline: UInt, bufSize: UInt, length: IntBuffer, infoLog: ByteBuffer
    ) = GLES31.nglGetProgramPipelineInfoLog(pipeline.toInt(), bufSize.toInt(), length.address, infoLog.address)

    override fun glGetProgramPipelineiv(
        pipeline: UInt, pname: Int, params: IntBuffer
    ) = GLES31.nglGetProgramPipelineiv(pipeline.toInt(), pname, params.address)

    override fun glGetProgramResourceIndex(
        program: UInt, programInterface: Int, name: String?
    ): UInt = GLES31.glGetProgramResourceIndex(program.toInt(), programInterface, name!!).toUInt()

    override fun glGetProgramResourceLocation(
        program: UInt, programInterface: Int, name: String?
    ): Int = GLES31.glGetProgramResourceLocation(program.toInt(), programInterface, name!!)

    override fun glGetProgramResourceName(
        program: UInt, programInterface: Int, index: UInt, bufSize: UInt, length: IntBuffer, name: ByteBuffer
    ) = GLES31.nglGetProgramResourceName(
        program.toInt(), programInterface, index.toInt(), bufSize.toInt(), length.address, name.address
    )

    override fun glGetProgramResourceiv(
        program: UInt,
        programInterface: Int,
        index: UInt,
        propCount: UInt,
        props: IntBufferRO,
        bufSize: UInt,
        length: IntBuffer,
        params: IntBuffer
    ) = GLES31.nglGetProgramResourceiv(
        program.toInt(),
        programInterface,
        index.toInt(),
        propCount.toInt(),
        props.address,
        bufSize.toInt(),
        length.address,
        params.address
    )

    override fun glGetTexLevelParameterfv(
        target: Int, level: Int, pname: Int, params: FloatBuffer
    ) = GLES31.nglGetTexLevelParameterfv(target, level, pname, params.address)

    override fun glGetTexLevelParameteriv(
        target: Int, level: Int, pname: Int, params: IntBuffer
    ) = GLES31.nglGetTexLevelParameteriv(target, level, pname, params.address)

    override fun glIsProgramPipeline(pipeline: UInt): Boolean = GLES31.glIsProgramPipeline(pipeline.toInt())

    override fun glMemoryBarrier(barriers: Int) = GLES31.glMemoryBarrier(barriers)

    override fun glMemoryBarrierByRegion(barriers: Int) = GLES31.glMemoryBarrierByRegion(barriers)

    override fun glProgramUniform1f(program: UInt, location: Int, v0: Float) =
        GLES31.glProgramUniform1f(program.toInt(), location, v0)

    override fun glProgramUniform1fv(
        program: UInt, location: Int, count: UInt, value: FloatBufferRO
    ) = GLES31.nglProgramUniform1fv(program.toInt(), location, count.toInt(), value.address)

    override fun glProgramUniform1i(program: UInt, location: Int, v0: Int) =
        GLES31.glProgramUniform1i(program.toInt(), location, v0)

    override fun glProgramUniform1iv(
        program: UInt, location: Int, count: UInt, value: IntBufferRO
    ) = GLES31.nglProgramUniform1iv(program.toInt(), location, count.toInt(), value.address)

    override fun glProgramUniform1ui(program: UInt, location: Int, v0: UInt) =
        GLES31.glProgramUniform1ui(program.toInt(), location, v0.toInt())

    override fun glProgramUniform1uiv(
        program: UInt, location: Int, count: UInt, value: IntBufferRO
    ) = GLES31.nglProgramUniform1uiv(program.toInt(), location, count.toInt(), value.address)

    override fun glProgramUniform2f(program: UInt, location: Int, v0: Float, v1: Float) =
        GLES31.glProgramUniform2f(program.toInt(), location, v0, v1)

    override fun glProgramUniform2fv(
        program: UInt, location: Int, count: UInt, value: FloatBufferRO
    ) = GLES31.nglProgramUniform2fv(program.toInt(), location, count.toInt(), value.address)

    override fun glProgramUniform2i(program: UInt, location: Int, v0: Int, v1: Int) =
        GLES31.glProgramUniform2i(program.toInt(), location, v0, v1)

    override fun glProgramUniform2iv(
        program: UInt, location: Int, count: UInt, value: IntBufferRO
    ) = GLES31.nglProgramUniform2iv(program.toInt(), location, count.toInt(), value.address)

    override fun glProgramUniform2ui(program: UInt, location: Int, v0: Int, v1: UInt) =
        GLES31.glProgramUniform2ui(program.toInt(), location, v0, v1.toInt())

    override fun glProgramUniform2uiv(
        program: UInt, location: Int, count: UInt, value: IntBufferRO
    ) = GLES31.nglProgramUniform2uiv(program.toInt(), location, count.toInt(), value.address)

    override fun glProgramUniform3f(
        program: UInt, location: Int, v0: Float, v1: Float, v2: Float
    ) = GLES31.glProgramUniform3f(program.toInt(), location, v0, v1, v2)

    override fun glProgramUniform3fv(
        program: UInt, location: Int, count: UInt, value: FloatBufferRO
    ) = GLES31.nglProgramUniform3fv(program.toInt(), location, count.toInt(), value.address)

    override fun glProgramUniform3i(
        program: UInt, location: Int, v0: Int, v1: Int, v2: Int
    ) = GLES31.glProgramUniform3i(program.toInt(), location, v0, v1, v2)

    override fun glProgramUniform3iv(
        program: UInt, location: Int, count: UInt, value: IntBufferRO
    ) = GLES31.nglProgramUniform3iv(program.toInt(), location, count.toInt(), value.address)

    override fun glProgramUniform3ui(
        program: UInt, location: Int, v0: Int, v1: Int, v2: UInt
    ) = GLES31.glProgramUniform3ui(program.toInt(), location, v0, v1, v2.toInt())

    override fun glProgramUniform3uiv(
        program: UInt, location: Int, count: UInt, value: IntBufferRO
    ) = GLES31.nglProgramUniform3uiv(program.toInt(), location, count.toInt(), value.address)

    override fun glProgramUniform4f(
        program: UInt, location: Int, v0: Float, v1: Float, v2: Float, v3: Float
    ) = GLES31.glProgramUniform4f(program.toInt(), location, v0, v1, v2, v3)

    override fun glProgramUniform4fv(
        program: UInt, location: Int, count: UInt, value: FloatBufferRO
    ) = GLES31.nglProgramUniform4fv(program.toInt(), location, count.toInt(), value.address)

    override fun glProgramUniform4i(
        program: UInt, location: Int, v0: Int, v1: Int, v2: Int, v3: Int
    ) = GLES31.glProgramUniform4i(program.toInt(), location, v0, v1, v2, v3)

    override fun glProgramUniform4iv(
        program: UInt, location: Int, count: UInt, value: IntBufferRO
    ) = GLES31.nglProgramUniform4iv(program.toInt(), location, count.toInt(), value.address)

    override fun glProgramUniform4ui(
        program: UInt, location: Int, v0: Int, v1: Int, v2: Int, v3: UInt
    ) = GLES31.glProgramUniform4ui(program.toInt(), location, v0, v1, v2, v3.toInt())

    override fun glProgramUniform4uiv(
        program: UInt, location: Int, count: UInt, value: IntBufferRO
    ) = GLES31.nglProgramUniform4uiv(program.toInt(), location, count.toInt(), value.address)

    override fun glProgramUniformMatrix2fv(
        program: UInt, location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO
    ) = GLES31.nglProgramUniformMatrix2fv(program.toInt(), location, count.toInt(), transpose, value.address)

    override fun glProgramUniformMatrix2x3fv(
        program: UInt, location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO
    ) = GLES31.nglProgramUniformMatrix2x3fv(program.toInt(), location, count.toInt(), transpose, value.address)

    override fun glProgramUniformMatrix2x4fv(
        program: UInt, location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO
    ) = GLES31.nglProgramUniformMatrix2x4fv(program.toInt(), location, count.toInt(), transpose, value.address)

    override fun glProgramUniformMatrix3fv(
        program: UInt, location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO
    ) = GLES31.nglProgramUniformMatrix3fv(program.toInt(), location, count.toInt(), transpose, value.address)

    override fun glProgramUniformMatrix3x2fv(
        program: UInt, location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO
    ) = GLES31.nglProgramUniformMatrix3x2fv(program.toInt(), location, count.toInt(), transpose, value.address)

    override fun glProgramUniformMatrix3x4fv(
        program: UInt, location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO
    ) = GLES31.nglProgramUniformMatrix3x4fv(program.toInt(), location, count.toInt(), transpose, value.address)

    override fun glProgramUniformMatrix4fv(
        program: UInt, location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO
    ) = GLES31.nglProgramUniformMatrix4fv(program.toInt(), location, count.toInt(), transpose, value.address)

    override fun glProgramUniformMatrix4x2fv(
        program: UInt, location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO
    ) = GLES31.nglProgramUniformMatrix4x2fv(program.toInt(), location, count.toInt(), transpose, value.address)

    override fun glProgramUniformMatrix4x3fv(
        program: UInt, location: Int, count: UInt, transpose: Boolean, value: FloatBufferRO
    ) = GLES31.nglProgramUniformMatrix4x3fv(program.toInt(), location, count.toInt(), transpose, value.address)

    override fun glSampleMaski(maskNumber: UInt, mask: Int) = GLES31.glSampleMaski(maskNumber.toInt(), mask)

    override fun glTexStorage2DMultisample(
        target: Int, samples: UInt, internalformat: Int, width: UInt, height: UInt, fixedsamplelocations: Boolean
    ) = GLES31.glTexStorage2DMultisample(
        target, samples.toInt(), internalformat, width.toInt(), height.toInt(), fixedsamplelocations
    )

    override fun glUseProgramStages(pipeline: UInt, stages: Int, program: UInt) =
        GLES31.glUseProgramStages(pipeline.toInt(), stages, program.toInt())

    override fun glValidateProgramPipeline(pipeline: UInt) = GLES31.glValidateProgramPipeline(pipeline.toInt())

    override fun glVertexAttribBinding(attribindex: UInt, bindingindex: UInt) =
        GLES31.glVertexAttribBinding(attribindex.toInt(), bindingindex.toInt())

    override fun glVertexAttribFormat(
        attribindex: UInt, size: Int, type: Int, normalized: Boolean, relativeoffset: UInt
    ) = GLES31.glVertexAttribFormat(attribindex.toInt(), size, type, normalized, relativeoffset.toInt())

    override fun glVertexAttribIFormat(
        attribindex: UInt, size: Int, type: Int, relativeoffset: UInt
    ) = GLES31.glVertexAttribIFormat(attribindex.toInt(), size, type, relativeoffset.toInt())

    override fun glVertexBindingDivisor(bindingindex: UInt, divisor: UInt) =
        GLES31.glVertexBindingDivisor(bindingindex.toInt(), divisor.toInt())
}