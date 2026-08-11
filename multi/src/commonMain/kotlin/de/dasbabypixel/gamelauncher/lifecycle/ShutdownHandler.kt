package de.dasbabypixel.gamelauncher.lifecycle

import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractThreadTask
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.api.util.concurrent.create
import de.dasbabypixel.gamelauncher.resource.ResourceTracker
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.system.exitProcess

@OptIn(ExperimentalAtomicApi::class)
object ShutdownHandler {
    private val shuttingDown = AtomicBoolean(false)

    fun shutdownGracefully() {
        if (!shuttingDown.compareAndSet(expectedValue = false, newValue = true)) return
        val shutdownTracker = ResourceTracker()
        Thread.create("ShutdownThread", { thread ->
            object : AbstractThreadTask(shutdownTracker, thread) {
                override fun run0() {

                    shutdownGracefullyPlatform()
                    ResourceTracker.global.exit()
                    stopTracking()
                    shutdownTracker.exit()
                }

                override fun cleanup0(): CompletableFuture<Unit>? = null
            }
        }).also { it.thread.start() }
    }

    fun shutdownByError(throwable: Throwable) {
        exitProcess(1)
    }
}

expect fun ShutdownHandler.shutdownGracefullyPlatform()
