package de.dasbabypixel.gamelauncher.lwjgl.opengl

import org.lwjgl.opengl.GL
import org.lwjgl.opengles.GLES
import org.lwjgl.system.Configuration

object LWJGLGLES : LWJGLGLES32() {
    init {
        Configuration.OPENGL_EXPLICIT_INIT.set(true)
        Configuration.OPENGLES_EXPLICIT_INIT.set(true)
        GL.create()
        val funs = GL.getFunctionProvider()!!
        GL.destroy()
        println(funs)
        GLES.create(funs)
    }
}
