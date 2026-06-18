package de.dasbabypixel.gamelauncher.impl

import de.dasbabypixel.gamelauncher.api.util.concurrent.configureThirdPartyThread
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.time.nanoTime
import de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j.LWJGLLogging
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis
import java.lang.Thread as JThread

class Main {
    companion object {
        val logger by getLogger()
        val startupNanos = nanoTime
    }
}

fun main() {
    JThread.setDefaultUncaughtExceptionHandler { thread, exception ->
        Main.logger.error("Uncaught Exception in {}", thread.name, exception)
        exitProcess(1)
    }
    JThread.currentThread().configureThirdPartyThread()

    measureTimeMillis { LWJGLLogging.init() }.let { time ->
        Main.logger.info("Initializing logging took {}ms", time)
    }

    val startupThread = StartupThread.create()

    startupThread.selectedWindowSystem.join().takeOverInitialThread()
}
