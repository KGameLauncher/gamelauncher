package de.dasbabypixel.gamelauncher.impl

import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractThreadTask
import de.dasbabypixel.gamelauncher.impl.window.WindowSystem
import de.dasbabypixel.gamelauncher.service.ServiceRegistry
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.util.concurrent.Thread

class StartupThread(
    val serviceRegistry: ServiceRegistry, thread: Thread
) : AbstractThreadTask(serviceRegistry.singleInstance(), serviceRegistry.singleInstance(), thread) {
    val selectedWindowSystem = CompletableFuture<WindowSystem>()
    override fun run0() {
        DesktopInitializer(serviceRegistry).init(this)

        stopTracking()
    }

    override fun cleanup0(): CompletableFuture<Unit>? {
        return null
    }

    companion object {
        fun create(
            serviceRegistry: ServiceRegistry
        ): StartupThread {
            return Thread.create(
                "StartupThread", taskFactory = { thread ->
                    StartupThread(
                        serviceRegistry, thread
                    )
                }).also { thread ->
                thread.start()
            }
        }
    }
}
