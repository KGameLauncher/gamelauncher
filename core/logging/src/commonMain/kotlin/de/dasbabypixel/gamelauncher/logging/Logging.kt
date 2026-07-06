package de.dasbabypixel.gamelauncher.logging

inline fun <reified T : Any> LoggingInstance.getLogger(): Logger = getLogger(T::class)
inline fun <reified T : Any> LoggingInstance.getLogger(marker: String): Logger =
    withDefaultMarker(getLogger<T>(), getMarker(marker))

inline fun <reified T : Any> T.getDirectLogger(instance: LoggingInstance): Logger =
    instance.getLogger<T>()

inline fun <reified T : Any> T.getDirectLogger(instance: LoggingInstance, marker: String): Logger =
    instance.getLogger<T>(marker)


inline fun <reified T : Any> T.getLogger(instance: LoggingInstance): Lazy<Logger> =
    lazy { getDirectLogger(instance) }

inline fun <reified T : Any> T.getLogger(instance: LoggingInstance, marker: String): Lazy<Logger> =
    lazy { getDirectLogger(instance, marker) }

