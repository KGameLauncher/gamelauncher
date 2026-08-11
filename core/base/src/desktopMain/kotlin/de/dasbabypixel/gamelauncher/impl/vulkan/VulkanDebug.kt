package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKInstance
import de.dasbabypixel.gamelauncher.lifecycle.ShutdownHandler
import de.dasbabypixel.gamelauncher.util.Config
import de.dasbabypixel.gamelauncher.util.GameException
import de.dasbabypixel.gamelauncher.util.debug.Debug
import org.lwjgl.vulkan.EXTDebugUtils
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT

object VulkanDebug {
    val logger by getVKLogger()

    val enableValidationLayers: Boolean = !Config.release() || Debug.debug
}

fun VKInstance.setupDebugMessenger() {
    this.setupDebugMessenger { severity, type, pCallbackData, pUserData ->
        val callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData)
        val message = callbackData.pMessageString()!!
        val typeString = when (type) {
            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT -> "validation"
            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT -> "performance"
            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT -> "general"
            else -> "UNKNOWN"
        }
        VulkanDebug.logger.error("Validation layer: Type {} Message: {}", typeString, message, GameException())
        ShutdownHandler.shutdownGracefully()
        0
    }
}