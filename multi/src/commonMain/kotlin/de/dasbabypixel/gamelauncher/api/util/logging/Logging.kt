package de.dasbabypixel.gamelauncher.api.util.logging

import kotlin.reflect.KClass

inline fun <reified T : Any> getLogger(): Logger = getLogger(T::class)

inline fun <reified T : Any> getLogger(marker: String): Logger = getLogger<T>().withDefaultMarker(marker)

fun Logger.withDefaultMarker(marker: String) = withDefaultMarker(getMarker(marker))

expect fun <T : Any> getLogger(cls: KClass<T>): Logger

expect fun getMarker(marker: String): Marker

expect fun Logger.withDefaultMarker(marker: Marker): Logger
