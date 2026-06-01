package de.dasbabypixel.gamelauncher.impl

import de.dasbabypixel.gamelauncher.api.util.logging.Logger
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j.LWJGLLogging
import kotlin.system.measureTimeMillis

object DesktopInitializer {
    private val logger: Logger by lazy { getLogger<DesktopInitializer>() }
    fun init() {
        measureTimeMillis { LWJGLLogging.init() }.let { time ->
            logger.info("Initializing logging took {}ms", time)
        }

        LWJGLLogging.startReader()
    }
}