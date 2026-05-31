package de.dasbabypixel.gamelauncher.api.util.logging

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream

interface PlatformLoggingInstance {
    fun log(msg: String)

    companion object
}

expect operator fun PlatformLoggingInstance.Companion.invoke(
    logger: Logger,
    printLocation: Boolean
): PlatformLoggingInstance

class LoggingPrintStream(
    target: Logger, printLocation: Boolean = true
) : PrintStream(OutputStreamConverter(target, printLocation), false, Charsets.UTF_8.name()) {

    class OutputStreamConverter(logger: Logger, printLocation: Boolean) : OutputStream() {
        private val platform = PlatformLoggingInstance(logger, printLocation)
        private val newLineCode = '\n'.code
        private val carriageReturnCode = '\r'.code
        private var carriage = false
        private val out = ByteArrayOutputStream()
        override fun write(b: Int) {
            val newLine = b == newLineCode
            if (newLine) {
                carriage = false
                val bytes = out.toByteArray()
                val string = bytes.toString(Charsets.UTF_8)
                out.reset()
                platform.log(string)
                return
            } else if (carriage) {
                out.write(carriageReturnCode)
            }
            carriage = b == carriageReturnCode
            if (carriage) return
            out.write(b)
        }
    }
}
