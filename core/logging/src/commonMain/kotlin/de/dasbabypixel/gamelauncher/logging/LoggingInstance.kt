package de.dasbabypixel.gamelauncher.logging

import kotlin.reflect.KClass

interface LoggingInstance {
    val platformPatternProvider: CustomPattern.PlatformProvider

    fun <T : Any> getLogger(cls: KClass<T>): Logger
    fun getMarker(marker: String): Marker
    fun withDefaultMarker(logger: Logger, marker: Marker): Logger
}
