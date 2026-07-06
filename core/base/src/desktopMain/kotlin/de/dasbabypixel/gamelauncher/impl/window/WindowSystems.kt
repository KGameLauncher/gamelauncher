package de.dasbabypixel.gamelauncher.impl.window

import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.window.glfw.GLFWWindowSystem
import de.dasbabypixel.gamelauncher.logging.LoggingInstance
import de.dasbabypixel.gamelauncher.service.ServiceRegistry
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class WindowSystems constructor(
    val tracker: ResourceTracker,
    val loggingInstance: LoggingInstance,
    val serviceRegistry: ServiceRegistry
) {
    private val lock = ReentrantLock()
    private var selected: WindowSystem? = null

    fun load() {
        lock.withLock {
            val windowSystem = GLFWWindowSystem(tracker, loggingInstance, serviceRegistry)

            windowSystem.init()
            selected = windowSystem
        }
    }

    fun selected(): WindowSystem = lock.withLock { selected!! }

    fun terminate(): CompletableFuture<Unit>? {
        lock.withLock {
            println("Terminate")
            return selected?.cleanupAsync()
        }
    }
}