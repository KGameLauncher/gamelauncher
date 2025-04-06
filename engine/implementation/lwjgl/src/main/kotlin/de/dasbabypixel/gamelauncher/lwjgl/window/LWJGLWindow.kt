package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.util.resource.GameResource
import de.dasbabypixel.gamelauncher.api.window.Window
import java.util.concurrent.CompletableFuture

interface LWJGLWindow : Window, GameResource {
    override val renderThread: LWJGLRenderThread
    fun changeRenderImplementation(renderImplementation: WindowRenderImplementation): CompletableFuture<Unit>
    fun title(name: String): CompletableFuture<Unit>
    fun position(x: Int, y: Int): CompletableFuture<Unit>
    fun framebufferSizeCallback(callback: (window: LWJGLWindow, width: Int, height: Int) -> Unit)
    fun requestCloseCallback(callback: (window: LWJGLWindow) -> Unit)
}