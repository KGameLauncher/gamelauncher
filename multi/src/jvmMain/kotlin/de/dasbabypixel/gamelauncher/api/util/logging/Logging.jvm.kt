package de.dasbabypixel.gamelauncher.api.util.logging

import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import kotlin.reflect.KClass

actual fun <T : Any> getLogger(cls: KClass<T>): Logger = LoggerFactory.getLogger(cls.java)
actual fun getMarker(marker: String): Marker = MarkerFactory.getMarker(marker)
actual fun Logger.withDefaultMarker(marker: Marker): Logger = MarkerLogger(this, marker)
