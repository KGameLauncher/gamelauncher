package de.dasbabypixel.gamelauncher.api.util.logging

import de.dasbabypixel.gamelauncher.api.util.logging.log4j.Log4jLevels
import de.dasbabypixel.gamelauncher.api.util.logging.slf4j.MarkerLogger
import de.dasbabypixel.gamelauncher.api.util.logging.slf4j.SLF4JLogger
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.MarkerManager
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintStream

private val walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
private val osc = LoggingPrintStream.OutputStreamConverter::class.java.name
private val drop = listOf(
    OutputStream::class,
    PrintStream::class,
    OutputStreamWriter::class,
    Throwable::class,
    ThreadGroup::class
).map { it.java.name }
    .plus(listOf("sun.nio.cs.StreamEncoder", $$"java.lang.Throwable$WrappedPrintStream")).toSet()

actual operator fun PlatformLoggingInstance.Companion.invoke(
    logger: Logger, printLocation: Boolean
): PlatformLoggingInstance = object : PlatformLoggingInstance {
    val logger = LogManager.getLogger(logger.name)!!

    override fun log(msg: String) {
        logger as SLF4JLogger
        val location = findCaller()
        val marker =
            if (logger.l is MarkerLogger) MarkerManager.getMarker(logger.l.marker.name) else null
        this.logger.atLevel(Log4jLevels.printStream).withMarker(marker).withLocation(location)
            .log(msg)
    }

    private fun findCaller(): StackTraceElement {
        return walker.walk { s ->
            s.dropWhile { f ->
                f.className != osc
            }.dropWhile { f ->
                f.className == osc
            }.dropWhile { f ->
                drop.contains(f.className)
            }.findFirst()
        }.map { it.toStackTraceElement() }.orElseThrow()
    }
}