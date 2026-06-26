package de.dasbabypixel.gamelauncher.api.lifecycle

import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractThreadTask
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.api.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.api.util.concurrent.create
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

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
        shutdownByErrorPlatform(throwable)
    }
}

expect fun ShutdownHandler.shutdownGracefullyPlatform()
expect fun ShutdownHandler.shutdownByErrorPlatform(throwable: Throwable)
