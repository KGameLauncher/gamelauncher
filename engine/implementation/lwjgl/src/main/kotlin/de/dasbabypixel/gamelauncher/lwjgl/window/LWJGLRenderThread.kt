package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.window.WindowRenderThread
import java.util.concurrent.CompletableFuture

interface LWJGLRenderThread : WindowRenderThread {
    override val window: LWJGLWindow
    val started: CompletableFuture<Unit>
}