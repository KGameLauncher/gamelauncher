package de.dasbabypixel.gamelauncher.lifecycle

import de.dasbabypixel.gamelauncher.resource.SimpleResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.AbstractThreadTask
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.util.concurrent.create
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
object ShutdownHandler {
    private val shuttingDown = AtomicBoolean(false)

    fun shutdownGracefully() {
        if (!shuttingDown.compareAndSet(expectedValue = false, newValue = true)) return
        val shutdownTracker = SimpleResourceTracker()
        Thread.create("ShutdownThread", { thread ->
            object : AbstractThreadTask(shutdownTracker, thread) {
                override fun run0() {
                    shutdownGracefullyPlatform()
                    SimpleResourceTracker.global.exit()
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
