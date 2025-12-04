package de.dasbabypixel.gamelauncher.lwjgl.opengl

import org.lwjgl.opengl.GL
import org.lwjgl.opengles.GLES
import org.lwjgl.system.Configuration
import org.lwjgl.system.FunctionProvider
import org.lwjgl.system.ThreadLocalUtil
import java.lang.invoke.MethodHandles

object LWJGLGLES : LWJGLGLES32() {
    private const val BACKEND_ES = false

    init {
        Configuration.OPENGL_EXPLICIT_INIT.set(true)
        Configuration.OPENGLES_EXPLICIT_INIT.set(true)

        val lookup = MethodHandles.privateLookupIn(GLES::class.java, MethodHandles.lookup())
        val glesFunctionProvider =
            lookup.findStaticSetter(GLES::class.java, "functionProvider", FunctionProvider::class.java)

        if (BACKEND_ES) {
            GL.create()
            ThreadLocalUtil.setFunctionMissingAddresses(0)
            GLES.create(GL.getFunctionProvider()!!)
//            glesFunctionProvider.invoke(GL.getFunctionProvider())
//            GL.destroy()
        } else {
            Configuration.OPENGLES_LIBRARY_NAME.set("OpenGL32")
            GL.create()
            ThreadLocalUtil.setFunctionMissingAddresses(0)
            GLES.create(GL.getFunctionProvider()!!)
//            glesFunctionProvider.invoke(GL.getFunctionProvider())
        }
    }

    fun unsetCapabilities() {
        if (BACKEND_ES) {
            GLES.setCapabilities(null)
        } else {
            GL.setCapabilities(null)
//            GLES.setCapabilities(null)
        }
    }

    fun createCapabilities() {
        if (BACKEND_ES) {
            GLES.createCapabilities()
        } else {
            GL.createCapabilities()
//            GLES.createCapabilities()
        }
    }
}
