package de.dasbabypixel.gamelauncher.util.logging

import kotlin.reflect.KClass

inline fun <reified T : Any> getDirectLogger(): Logger = getDirectLogger(T::class)
inline fun <reified T : Any> getDirectLogger(marker: String): Logger = getDirectLogger<T>().withDefaultMarker(marker)

inline fun <reified T : Any> T.getDirectLogger() = getDirectLogger(T::class)
inline fun <reified T : Any> T.getDirectLogger(marker: String) = getDirectLogger(T::class).withDefaultMarker(marker)

inline fun <reified T : Any> T.getLogger() = lazy { getDirectLogger() }
inline fun <reified T : Any> T.getLogger(marker: String) = lazy { getDirectLogger(marker) }

inline fun <reified T : Any> getLogger() = lazy { getDirectLogger<T>() }
inline fun <reified T : Any> getLogger(marker: String) = lazy { getDirectLogger<T>(marker) }

fun Logger.withDefaultMarker(marker: String) = withDefaultMarker(getMarker(marker))

expect fun <T : Any> getDirectLogger(cls: KClass<T>): Logger

expect fun getMarker(marker: String): Marker

expect fun Logger.withDefaultMarker(marker: Marker): Logger
