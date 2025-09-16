package de.dasbabypixel.gamelauncher.impl.util.logging

import de.dasbabypixel.gamelauncher.api.util.Registry.instance
import org.slf4j.Logger
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream

class LoggingPrintStream(
    target: Logger, printLocation: Boolean = true
) : PrintStream(OutputStreamConverter(target, printLocation), false, Charsets.UTF_8.name()) {

    interface Platform {
        fun createInstance(logger: Logger, printLocation: Boolean): PlatformInstance
    }

    interface PlatformInstance {
        fun log(message: String)
    }

    class OutputStreamConverter(logger: Logger, printLocation: Boolean) : OutputStream() {
        private val platform = instance<Platform>().createInstance(logger, printLocation)
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
