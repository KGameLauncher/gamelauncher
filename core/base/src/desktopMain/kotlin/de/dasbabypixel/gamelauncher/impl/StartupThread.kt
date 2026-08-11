package de.dasbabypixel.gamelauncher.impl

import de.dasbabypixel.gamelauncher.impl.api.util.concurrent.DisruptorMPSC
import de.dasbabypixel.gamelauncher.impl.window.WindowSystem
import de.dasbabypixel.gamelauncher.resource.SimpleResourceTracker
import de.dasbabypixel.gamelauncher.service.ServiceRegistry
import de.dasbabypixel.gamelauncher.util.concurrent.AbstractThreadTask
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.util.concurrent.EfficientMPSC
import de.dasbabypixel.gamelauncher.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.util.concurrent.create

class StartupThread(thread: Thread) : AbstractThreadTask(SimpleResourceTracker.global, thread) {
    val selectedWindowSystem = CompletableFuture<WindowSystem>()
    override fun run0() {
        DesktopInitializer.init(this)

        stopTracking()
    }

    override fun cleanup0(): CompletableFuture<Unit>? {
        return null
    }

    companion object {
        init {
            ServiceRegistry.global.register<EfficientMPSC> { DisruptorMPSC }
        }

        fun create(): StartupThread {
            return Thread.create("StartupThread", ::StartupThread).also { thread ->
                thread.start()
            }
        }
    }
}
