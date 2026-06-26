package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkExtensionProperties
import org.lwjgl.vulkan.VkPhysicalDevice
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures
import org.lwjgl.vulkan.VkPhysicalDeviceProperties
import org.lwjgl.vulkan.VkQueueFamilyProperties
import java.nio.ByteBuffer

class VKPhysicalDevice(val instance: VKInstance, val device: VkPhysicalDevice) {
    fun properties(properties: VkPhysicalDeviceProperties) {
        VK10.vkGetPhysicalDeviceProperties(device, properties)
    }

    fun features(features: VkPhysicalDeviceFeatures) {
        VK10.vkGetPhysicalDeviceFeatures(device, features)
    }

    fun getQueueFamilyProperties(stack: MemoryStack): VkQueueFamilyProperties.Buffer {
        val pCount = stack.mallocInt(1)
        VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, pCount, null)
        val pProperties = VkQueueFamilyProperties.malloc(pCount.get(0), stack)
        VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, pCount, pProperties)
        return pProperties
    }

    fun getExtensions(stack: MemoryStack): VkExtensionProperties.Buffer {
        val pCount = stack.mallocInt(1)
        VK10.vkEnumerateDeviceExtensionProperties(device, null as ByteBuffer?, pCount, null)
        val pProperties = VkExtensionProperties.malloc(pCount.get(0), stack)
        VK10.vkEnumerateDeviceExtensionProperties(device, null as ByteBuffer?, pCount, pProperties)
        return pProperties
    }
}