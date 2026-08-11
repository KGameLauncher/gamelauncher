package de.dasbabypixel.gamelauncher.logging

import kotlin.reflect.KClass

expect val loggingInstance: Lazy<LoggingInstance>

inline fun <reified T : Any> T.getLogger(): Lazy<Logger> = lazy { loggingInstance.value.getLogger<T>() }
inline fun <reified T : Any> T.getLogger(marker: String): Lazy<Logger> =
    lazy { loggingInstance.value.getLogger<T>(marker) }

inline fun getMarker(marker: String): Lazy<Marker> {
    return lazy { loggingInstance.value.getMarker(marker) }
}

inline fun Logger.withDefaultMarker(marker: String): Logger = withDefaultMarker(getMarker(marker))
inline fun Logger.withDefaultMarker(marker: Marker): Logger = loggingInstance.value.withDefaultMarker(this, marker)
inline fun Logger.withDefaultMarker(marker: Lazy<Marker>) = withDefaultMarker(marker.value)

inline fun Lazy<Logger>.withDefaultMarker(marker: String): Lazy<Logger> = withDefaultMarker(getMarker(marker))
inline fun Lazy<Logger>.withDefaultMarker(marker: Marker): Lazy<Logger> =
    lazy { loggingInstance.value.withDefaultMarker(value, marker) }

inline fun Lazy<Logger>.withDefaultMarker(marker: Lazy<Marker>): Lazy<Logger> =
    lazy { loggingInstance.value.withDefaultMarker(value, marker.value) }

object Inst : LoggingInstance {
    override val platformPatternProvider: CustomPattern.PlatformProvider
        get() = TODO("Not yet implemented")

    override fun <T : Any> getLogger(cls: KClass<T>): Logger {
        TODO("Not yet implemented")
    }

    override fun getMarker(marker: String): Marker {
        TODO("Not yet implemented")
    }

    override fun withDefaultMarker(
        logger: Logger, marker: Marker
    ): Logger {
        TODO("Not yet implemented")
    }

}