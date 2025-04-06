package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.window.WindowRenderThread

interface LWJGLRenderThread : WindowRenderThread {
    override val window: LWJGLWindow
}