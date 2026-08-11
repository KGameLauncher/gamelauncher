package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanDeviceSelection.SelectedPhysicalDevice
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKDevice
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKDeviceQueueCreateInfo
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKQueue
import de.dasbabypixel.gamelauncher.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkPhysicalDeviceProperties
import org.lwjgl.vulkan.VkQueue

class VulkanLogicalDevice(
    tracker: ResourceTracker, val device: VKDevice, val graphicsQueue: VKQueue, val instance: VulkanInstance
) : AbstractGameResource(tracker) {

    override fun cleanup0(): CompletableFuture<Unit> {
        return device.cleanupAsync()
    }

    fun msaaSamples(properties: VkPhysicalDeviceProperties): Int {
        val counts =
            properties.limits().framebufferColorSampleCounts() and properties.limits().framebufferDepthSampleCounts()
        if ((counts and VK10.VK_SAMPLE_COUNT_64_BIT) != 0) return VK10.VK_SAMPLE_COUNT_64_BIT
        if ((counts and VK10.VK_SAMPLE_COUNT_32_BIT) != 0) return VK10.VK_SAMPLE_COUNT_32_BIT
        if ((counts and VK10.VK_SAMPLE_COUNT_16_BIT) != 0) return VK10.VK_SAMPLE_COUNT_16_BIT
        if ((counts and VK10.VK_SAMPLE_COUNT_8_BIT) != 0) return VK10.VK_SAMPLE_COUNT_8_BIT
        if ((counts and VK10.VK_SAMPLE_COUNT_4_BIT) != 0) return VK10.VK_SAMPLE_COUNT_4_BIT
        if ((counts and VK10.VK_SAMPLE_COUNT_2_BIT) != 0) return VK10.VK_SAMPLE_COUNT_2_BIT
        return VK10.VK_SAMPLE_COUNT_1_BIT
    }

    fun waitIdle() = device.waitIdle()

    companion object {
        fun create(
            tracker: ResourceTracker, physicalDevice: SelectedPhysicalDevice, instance: VulkanInstance
        ): VulkanLogicalDevice {
            return MemoryStack.stackPush().use { stack ->
                val device = VKDevice.create(tracker,
                    physicalDevice.device.instance,
                    physicalDevice.device,
                    listOf(VKDeviceQueueCreateInfo(physicalDevice.graphicsQueueIndex, listOf(0.5F))),
                    physicalDevice.selectedExtensions)

                val pQueue = stack.mallocPointer(1)
                VK10.vkGetDeviceQueue(device.device, physicalDevice.graphicsQueueIndex, 0, pQueue)
                val graphicsQueue = VKQueue(VkQueue(pQueue.get(0), device.device), physicalDevice.graphicsQueueIndex)

                VulkanLogicalDevice(tracker, device, graphicsQueue, instance)
            }
        }
    }
}
