package de.dasbabypixel.gamelauncher.logging

inline fun <reified T : Any> LoggingInstance.getLogger(): Logger = getLogger(T::class)
inline fun <reified T : Any> LoggingInstance.getLogger(marker: String): Logger =
    withDefaultMarker(getLogger<T>(), getMarker(marker))
