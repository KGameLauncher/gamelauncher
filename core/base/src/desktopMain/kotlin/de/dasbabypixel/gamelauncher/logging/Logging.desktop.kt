package de.dasbabypixel.gamelauncher.logging

import de.dasbabypixel.gamelauncher.impl.api.util.logging.slf4j.MarkerLogger
import de.dasbabypixel.gamelauncher.impl.api.util.logging.slf4j.SLF4JLogger
import de.dasbabypixel.gamelauncher.impl.api.util.logging.slf4j.SLF4JMarker
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import kotlin.reflect.KClass

actual val loggingInstance: Lazy<LoggingInstance> = lazy { SLF4JLoggingInstance }

object SLF4JLoggingInstance : LoggingInstance {
    override val platformPatternProvider: CustomPattern.PlatformProvider
        get() = TODO("Not yet implemented")

    override fun <T : Any> getLogger(cls: KClass<T>): Logger {
        val c = if (cls.isCompanion) cls.java.declaringClass.kotlin else cls
        return SLF4JLogger(LoggerFactory.getLogger(c.java))
    }

    override fun getMarker(marker: String): Marker {
        return SLF4JMarker(MarkerFactory.getMarker(marker))
    }

    override fun withDefaultMarker(
        logger: Logger, marker: Marker
    ): Logger {
        return SLF4JLogger(MarkerLogger((logger as SLF4JLogger).l, (marker as SLF4JMarker).m))
    }
}
