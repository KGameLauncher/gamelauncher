package de.dasbabypixel.gamelauncher.lwjgl.window

interface LWJGLWindowImpl : LWJGLWindow {
    val implName: String
    val id: Int
    fun startRendering(): RenderingInstance

    interface RenderingInstance {
        /**
         * Swaps the framebuffers. This will return false if the window has been destroyed
         */
        fun swapBuffers(): Boolean
        fun stopRendering()
    }
}
