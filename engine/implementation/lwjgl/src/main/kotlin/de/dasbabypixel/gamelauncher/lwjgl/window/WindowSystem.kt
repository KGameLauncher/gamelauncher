package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.config.Config
import de.dasbabypixel.gamelauncher.api.util.concurrent.currentThread
import de.dasbabypixel.gamelauncher.api.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.lwjgl.util.concurrent.InitialThread
import de.dasbabypixel.gamelauncher.lwjgl.window.glfw.GLFWWindowSystem
import de.dasbabypixel.gamelauncher.lwjgl.window.sdl.SDLWindowSystem
import java.util.concurrent.CompletableFuture

abstract class WindowSystem : AbstractGameResource() {
    protected val windows: MutableSet<LWJGLWindow> = HashSet()

    protected abstract fun createWindow0(): LWJGLWindow

    fun createWindow(): LWJGLWindow = createWindow0().apply {
        windows.add(this)
        cleanupFuture.thenRun { windows.remove(this) }
    }

    override fun cleanup0(): CompletableFuture<Unit>? {
        return CompletableFuture.completedFuture(Unit)
    }

    internal fun track() {
        super.track(currentThread)
    }

    /**
     * We have to disable autoTrack because the window system is initialized before the initial thread is set.
     */
    override val autoTrack: Boolean
        get() = false

    abstract fun initThread(): InitialThread.Implementation?

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