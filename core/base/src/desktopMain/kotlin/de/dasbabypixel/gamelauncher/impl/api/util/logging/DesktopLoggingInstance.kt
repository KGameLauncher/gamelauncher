package de.dasbabypixel.gamelauncher.impl.api.util.logging

import de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j.Log4jLevels
import de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j.Log4jPatternPlatformProvider
import de.dasbabypixel.gamelauncher.impl.api.util.logging.slf4j.MarkerLogger
import de.dasbabypixel.gamelauncher.impl.api.util.logging.slf4j.SLF4JLogger
import de.dasbabypixel.gamelauncher.impl.api.util.logging.slf4j.SLF4JMarker
import de.dasbabypixel.gamelauncher.logging.CustomPattern
import de.dasbabypixel.gamelauncher.logging.CustomPatterns
import de.dasbabypixel.gamelauncher.logging.JvmLoggingInstance
import de.dasbabypixel.gamelauncher.logging.Logger
import de.dasbabypixel.gamelauncher.logging.LoggingPrintStream
import de.dasbabypixel.gamelauncher.logging.Marker
import de.dasbabypixel.gamelauncher.logging.PatternRegistry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.MarkerManager
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintStream
import kotlin.reflect.KClass

class DesktopLoggingInstance : JvmLoggingInstance {
    val patternRegistry = PatternRegistry()
    val customPatterns = CustomPatterns(patternRegistry)
    override val platformPatternProvider: Log4jPatternPlatformProvider =
        Log4jPatternPlatformProvider(customPatterns)

    init {
        platformPatternProvider.register(patternRegistry)
    }

    private val walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
    private val osc = LoggingPrintStream.OutputStreamConverter::class.java.name
    private val drop = listOf(
        OutputStream::class,
        PrintStream::class,
        OutputStreamWriter::class,
        Throwable::class,
        ThreadGroup::class
    ).map { it.java.name }
        .plus(listOf("sun.nio.cs.StreamEncoder", $$"java.lang.Throwable$WrappedPrintStream"))
        .toSet()

    constructor()

    override fun prepareLocationLogger(logger: Logger): JvmLoggingInstance.LocationLogger {
        logger as SLF4JLogger
        val marker =
            if (logger.l is MarkerLogger) MarkerManager.getMarker(logger.l.marker.name) else null
        val l = LogManager.getLogger(logger.name)!!
        return object : JvmLoggingInstance.LocationLogger {
            override fun log(msg: String) {
                val location = findCaller()
                l.atLevel(Log4jLevels.PRINT_STREAM).withMarker(marker).withLocation(location)
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
    }

    override fun <T : Any> getLogger(cls: KClass<T>): Logger {
        val c = if (cls.isCompanion) cls.java.declaringClass.kotlin else cls
        return SLF4JLogger(LoggerFactory.getLogger(c.java), this)
    }

    override fun getMarker(marker: String): Marker = SLF4JMarker(MarkerFactory.getMarker(marker))

    override fun withDefaultMarker(
        logger: Logger, marker: Marker
    ): Logger =
        SLF4JLogger(MarkerLogger((logger as SLF4JLogger).l, (marker as SLF4JMarker).m), this)
}
