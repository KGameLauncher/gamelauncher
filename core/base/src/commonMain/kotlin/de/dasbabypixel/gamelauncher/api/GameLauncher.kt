package de.dasbabypixel.gamelauncher.api

import de.dasbabypixel.gamelauncher.api.lifecycle.ShutdownHandler
import de.dasbabypixel.gamelauncher.logging.LoggingInstance
import de.dasbabypixel.gamelauncher.service.ServiceRegistry
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker

class GameLauncher(
    val serviceRegistry: ServiceRegistry
) {
    val loggingInstance: LoggingInstance = serviceRegistry.singleInstance()
    val resourceTracker: ResourceTracker = serviceRegistry.singleInstance()
    private val shutdownHandler = ShutdownHandler(this)

    fun shutdownGracefully() = shutdownHandler.shutdownGracefully()

    fun shutdownByError(throwable: Throwable) = shutdownHandler.shutdownByError(throwable)

    companion object {
        fun create() {
            val serviceRegistry = ServiceRegistry()

        }
    }
}