package de.dasbabypixel.gamelauncher.logging

interface JvmLoggingInstance : LoggingInstance {
    fun prepareLocationLogger(logger: Logger): LocationLogger

    interface LocationLogger {
        fun log(msg: String)
    }
}