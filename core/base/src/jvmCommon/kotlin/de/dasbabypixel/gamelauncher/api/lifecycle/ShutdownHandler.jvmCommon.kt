package de.dasbabypixel.gamelauncher.api.lifecycle

import kotlin.system.exitProcess

actual fun ShutdownHandler.shutdownByErrorPlatform(
    throwable: Throwable
) {
    throwable.printStackTrace()
    exitProcess(1)
}
