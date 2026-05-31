package de.dasbabypixel.gamelauncher.api.util.logging

import de.dasbabypixel.gamelauncher.api.util.logging.slf4j.MarkerLogger
import de.dasbabypixel.gamelauncher.api.util.logging.slf4j.SLF4JLogger
import de.dasbabypixel.gamelauncher.api.util.logging.slf4j.SLF4JMarker
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import kotlin.reflect.KClass

actual fun <T : Any> getLogger(cls: KClass<T>): Logger =
    SLF4JLogger(LoggerFactory.getLogger(cls.java))

actual fun getMarker(marker: String): Marker = SLF4JMarker(MarkerFactory.getMarker(marker))
actual fun Logger.withDefaultMarker(marker: Marker): Logger =
    SLF4JLogger(MarkerLogger((this as SLF4JLogger).l, (marker as SLF4JMarker).m))
