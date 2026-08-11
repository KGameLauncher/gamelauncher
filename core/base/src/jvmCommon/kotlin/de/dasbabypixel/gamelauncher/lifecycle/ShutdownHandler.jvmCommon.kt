package de.dasbabypixel.gamelauncher.lifecycle

import kotlin.system.exitProcess

actual fun ShutdownHandler.shutdownByErrorPlatform(
    throwable: Throwable
) {
    throwable.printStackTrace()
    exitProcess(1)
}
