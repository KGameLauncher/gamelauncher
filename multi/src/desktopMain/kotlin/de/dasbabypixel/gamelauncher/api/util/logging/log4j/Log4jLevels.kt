package de.dasbabypixel.gamelauncher.api.util.logging.log4j

import org.apache.logging.log4j.Level

object Log4jLevels {
    val printStream: Level = Level.forName("PRINT_STREAM", 250)

    fun init() {}
}
