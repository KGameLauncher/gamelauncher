package de.dasbabypixel.gamelauncher.api.util.logging

import kotlin.reflect.KClass

actual fun <T : Any> getLogger(cls: KClass<T>): Logger {
    TODO("Not yet implemented")
}

actual fun getMarker(marker: String): Marker {
    TODO("Not yet implemented")
}

actual fun Logger.withDefaultMarker(marker: Marker): Logger {
    TODO("Not yet implemented")
}
