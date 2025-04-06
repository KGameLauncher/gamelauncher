package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.config.Config
import de.dasbabypixel.gamelauncher.lwjgl.util.concurrent.InitialThread
import de.dasbabypixel.gamelauncher.lwjgl.window.glfw.GLFWWindowSystem
import de.dasbabypixel.gamelauncher.lwjgl.window.sdl.SDLWindowSystem

interface WindowSystem {

    fun createWindow(): LWJGLWindow

    fun initThread(): InitialThread.Implementation?

    companion object {
        private val windowSystemName = Config.WINDOW_SYSTEM.value
        private val systems = HashMap<String, Function0<WindowSystem>>()

        init {
            systems["sdl"] = { SDLWindowSystem }
            systems["glfw"] = { GLFWWindowSystem }
        }

        val windowSystem = systems[windowSystemName]?.invoke()
            ?: error("No window system named $windowSystemName exists: (${systems.keys})")
    }
}