package de.dasbabypixel.gamelauncher.lwjgl.window.glfw

import de.dasbabypixel.gamelauncher.lwjgl.util.concurrent.InitialThread
import de.dasbabypixel.gamelauncher.lwjgl.window.LWJGLWindow
import de.dasbabypixel.gamelauncher.lwjgl.window.WindowSystem

object GLFWWindowSystem : WindowSystem() {
    override fun createWindow0(): LWJGLWindow {
        val window = GLFWWindow(null)
        window.create()
        window.renderThread.start()
        return window
    }

    override fun initThread(): InitialThread.Implementation {
        return GLFWThreadImplementation
    }
}