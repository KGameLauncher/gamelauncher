package de.dasbabypixel.gamelauncher.impl.window

import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.window.glfw.GLFWWindowSystem
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object WindowSystems {
    private val lock = ReentrantLock()
    private var selected: WindowSystem? = null

    fun load() {
        lock.withLock {
            val windowSystem = GLFWWindowSystem()

            windowSystem.init()
            selected = windowSystem
        }
    }

    fun selected(): WindowSystem = selected!!

    fun terminate(): CompletableFuture<Unit>? {
        lock.withLock {
            return selected?.cleanupAsync()
        }
    }
}