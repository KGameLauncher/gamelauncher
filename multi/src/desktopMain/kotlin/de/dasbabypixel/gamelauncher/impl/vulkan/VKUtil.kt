package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger

object VKUtil {
    private val logger by getVKLogger()
    fun selectExtensions(
        type: String,
        availableExtensions: Set<String>,
        requiredExtensions: Set<String>,
        optionalExtensions: Set<String>
    ): Set<String> {
        val missingExtensions = mutableSetOf<String>()
        requiredExtensions.forEach { if (!availableExtensions.contains(it)) missingExtensions.add(it) }

        if (missingExtensions.isNotEmpty()) {
            throw GameException("Missing Vulkan $type Extensions: $missingExtensions")
        }
        requiredExtensions.forEach {
            logger.debug("Using required $type extension {}", it)
        }

        val selectExtensions = mutableSetOf<String>()
        selectExtensions.addAll(requiredExtensions)
        selectExtensions.addAll(optionalExtensions.filter { availableExtensions.contains(it) }
            .also {
                logger.debug("Using optional $type extensions: {}", it)
            })
        return selectExtensions
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Any.getVKLogger() = getLogger("Vulkan")
