package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkPhysicalDevice
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures
import org.lwjgl.vulkan.VkPhysicalDeviceProperties

class VKPhysicalDevice(private val device: VkPhysicalDevice) {

    fun properties(stack: MemoryStack): Properties {
        val properties = VkPhysicalDeviceProperties.malloc(stack)
        VK10.vkGetPhysicalDeviceProperties(device, properties)
        return Properties(properties)
    }

    fun features(stack: MemoryStack): Features {
        return Features(VkPhysicalDeviceFeatures.malloc(stack).also {
            VK10.vkGetPhysicalDeviceFeatures(device, it)
        })
    }

    class Features(val features: VkPhysicalDeviceFeatures) {
        val geometryShader: Boolean get() = features.geometryShader()
    }

    class Properties(val properties: VkPhysicalDeviceProperties) {
        val name: String get() = properties.deviceNameString()
        val deviceType: Int get() = properties.deviceType()
    }
}