package de.dasbabypixel.gamelauncher.lwjgl.window.sdl

import de.dasbabypixel.gamelauncher.lwjgl.util.concurrent.InitialThread
import de.dasbabypixel.gamelauncher.lwjgl.window.LWJGLWindow
import de.dasbabypixel.gamelauncher.lwjgl.window.WindowSystem

object SDLWindowSystem : WindowSystem() {
    override fun createWindow0(): LWJGLWindow {
        val window = SDLWindow()
        window.create().join()
        window.renderThread.start()
        return window
    }

    override fun initThread(): InitialThread.Implementation {
        return SDLThreadImplementation
    }
}