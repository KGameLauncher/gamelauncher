package de.dasbabypixel.gamelauncher.lwjgl.window

interface LWJGLWindowImpl : LWJGLWindow {
    val implName: String
    val id: Int
    fun startRendering(): RenderingInstance

    interface RenderingInstance {
        fun swapBuffers()
        fun stopRendering()
    }
}