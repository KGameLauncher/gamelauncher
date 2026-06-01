package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.logging.Logger
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.logging.withDefaultMarker

internal inline fun <reified T : Any> T.getVKLogger(): Logger {
    if (T::class.isCompanion) return getLogger(T::class.java.declaringClass.kotlin).withDefaultMarker(
        "Vulkan")
    return getLogger("Vulkan")
}

@JvmName("vkValidateExplicit")
internal fun vkValidate(res: Int) {
    if (res != 0) throw GameException("Error in vulkan")
}

@JvmName("vkValidate")
internal fun Int.vkValidate() = vkValidate(this)
