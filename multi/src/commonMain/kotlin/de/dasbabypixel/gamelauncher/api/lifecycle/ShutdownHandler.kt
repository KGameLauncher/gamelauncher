package de.dasbabypixel.gamelauncher.api.lifecycle

import kotlin.system.exitProcess

object ShutdownHandler {
    fun shutdownGracefully() {
        exitProcess(0)
    }

    fun shutdownByError(throwable: Throwable) {
        exitProcess(1)
    }
}
