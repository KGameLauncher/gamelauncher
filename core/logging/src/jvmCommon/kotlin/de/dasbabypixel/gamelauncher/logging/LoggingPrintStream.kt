package de.dasbabypixel.gamelauncher.logging

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream

class LoggingPrintStream(
    target: Logger
) : PrintStream(
    OutputStreamConverter(target.loggingInstance as JvmLoggingInstance, target),
    false,
    Charsets.UTF_8.name()
) {

    class OutputStreamConverter(
        loggingInstance: JvmLoggingInstance,
        logger: Logger,
    ) : OutputStream() {
        private val platform = loggingInstance.prepareLocationLogger(logger)
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
