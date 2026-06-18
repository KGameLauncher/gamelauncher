package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKInstance
import org.lwjgl.vulkan.EXTDebugUtils
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT

object VKInitializerDebug {
    val logger by getVKLogger()
    fun setupDebugMessenger(instance: VKInstance) {
        instance.setupDebugMessenger { severity, type, pCallbackData, pUserData ->
            val callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData)
            val message = callbackData.pMessageString()!!
            val typeString = when (type) {
                EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT -> "validation"
                EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT -> "performance"
                EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT -> "general"
                else -> "ILLEGAL"
            }
            logger.error("Validation layer: Type {} Message: {}",
                typeString,
                message,
                GameException())
            0
        }
    }
}