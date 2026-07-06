package de.dasbabypixel.gamelauncher.impl

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.api.util.concurrent.EfficientMPSC
import de.dasbabypixel.gamelauncher.impl.api.util.concurrent.DisruptorMPSC
import de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j.LWJGLLogging
import de.dasbabypixel.gamelauncher.logging.Logger
import de.dasbabypixel.gamelauncher.logging.LoggingInstance
import de.dasbabypixel.gamelauncher.logging.getLogger
import de.dasbabypixel.gamelauncher.service.ServiceRegistry
import de.dasbabypixel.gamelauncher.util.concurrent.configureThirdPartyThread
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.time.nanoTime
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis
import java.lang.Thread as JThread

class Main {
    companion object {
        val startupNanos = nanoTime
    }
}

fun main() {
    Main.startupNanos
    val serviceRegistry = ServiceRegistry(true)
    serviceRegistry.register { ResourceTracker(true) }
    val logger: Logger
    measureTimeMillis {
        val logging =
            LWJGLLogging(serviceRegistry.singleInstance(), serviceRegistry)
        serviceRegistry.register { logging }
        serviceRegistry.register<LoggingInstance> { logging.loggingInstance }
        logger = logging.loggingInstance.getLogger<Main>()
    }.also { time ->
        logger.info("Initializing logging took {}ms", time)
    }
    serviceRegistry.register<EfficientMPSC> { DisruptorMPSC }

    JThread.setDefaultUncaughtExceptionHandler { thread, exception ->
        logger.error("Uncaught Exception in {}", thread.name, exception)
        exitProcess(1)
    }
    JThread.currentThread().configureThirdPartyThread()

    val startupThread = StartupThread.create(serviceRegistry)

    serviceRegistry.register<GameLauncher> { GameLauncher(serviceRegistry) }

    startupThread.selectedWindowSystem.join().takeOverInitialThread()
}
