package de.dasbabypixel.gamelauncher.api.lifecycle

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractThreadTask
import de.dasbabypixel.gamelauncher.logging.Logger
import de.dasbabypixel.gamelauncher.service.ServiceRegistry
import de.dasbabypixel.gamelauncher.util.GameException
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.util.concurrent.create
import de.dasbabypixel.gamelauncher.util.resource.GameResource
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class ShutdownHandler(val gameLauncher: GameLauncher) {
    private val shuttingDown = AtomicBoolean(false)

    fun shutdownGracefully() {
        if (!shuttingDown.compareAndSet(expectedValue = false, newValue = true)) return
        val shutdownTracker = ResourceTracker(true)
        Thread.create("ShutdownThread", { thread ->
            object : AbstractThreadTask(gameLauncher.loggingInstance, shutdownTracker, thread) {
                override fun run0() {
                    shutdownGracefullyPlatform()
                    gameLauncher.resourceTracker.exitAndValidate(logger)
                    stopTracking()
                    shutdownTracker.exitAndValidate(logger)
                    println("Shutdown complete")
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

fun ResourceTracker.exitAndValidate(logger: Logger) {
    exit().forEach { resourceLeak ->
        val resource = resourceLeak.resource
        if (resource is GameResource.StackCapable) {
            val ex = GameException("Stack: ${resource.creationThreadName}")
            resource.creationStack?.let { ex.stackTrace = it }

            logger.error("Memory Leak: {}", resource, ex)
        } else {
            logger.error("Memory Leak: {}", resource)
        }
    }
}
