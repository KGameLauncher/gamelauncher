package de.dasbabypixel.gamelauncher.impl

import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractThreadTask
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.api.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.api.util.concurrent.create
import de.dasbabypixel.gamelauncher.impl.window.WindowSystem

class StartupThread(thread: Thread) : AbstractThreadTask(ResourceTracker.global, thread) {
    val selectedWindowSystem = CompletableFuture<WindowSystem>()
    override fun run0() {
        DesktopInitializer.init(this)

        stopTracking()
    }

    override fun cleanup0(): CompletableFuture<Unit>? {
        return null
    }

    companion object {
        fun create(): StartupThread {
            return Thread.create("StartupThread", ::StartupThread).also { thread ->
                thread.start()
            }
        }
    }
}
