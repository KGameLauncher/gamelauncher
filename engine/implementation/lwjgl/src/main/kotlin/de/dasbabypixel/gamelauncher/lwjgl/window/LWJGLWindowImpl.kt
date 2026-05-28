package de.dasbabypixel.gamelauncher.lwjgl.window

interface LWJGLWindowImpl : LWJGLWindow {
    val implName: String
    val id: Int

    /**
     * Called by the render thread when rendering begins. Can be used by the window implementation to do work on the render thread before the rendering begins
     */
    fun startRendering()
}
