package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanDeviceSelection.SelectedPhysicalDevice
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKDevice
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKDeviceQueueCreateInfo
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKQueue
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkQueue

class VulkanLogicalDevice(
    tracker: ResourceTracker,
    val device: VKDevice,
    val graphicsQueue: VKQueue,
    val instance: VulkanInstance
) : AbstractGameResource(tracker) {

    override fun cleanup0(): CompletableFuture<Unit> {
        return device.cleanupAsync()
    }

    fun waitIdle() = device.waitIdle()

    companion object {
        fun create(
            tracker: ResourceTracker,
            physicalDevice: SelectedPhysicalDevice,
            instance: VulkanInstance
        ): VulkanLogicalDevice {
            return MemoryStack.stackPush().use { stack ->
                val device = VKDevice.create(tracker,
                    physicalDevice.device.instance,
                    physicalDevice.device,
                    listOf(VKDeviceQueueCreateInfo(physicalDevice.graphicsQueueIndex,
                        listOf(0.5F))),
                    physicalDevice.selectedExtensions)

                val pQueue = stack.mallocPointer(1)
                VK10.vkGetDeviceQueue(device.device, physicalDevice.graphicsQueueIndex, 0, pQueue)
                val graphicsQueue = VKQueue(VkQueue(pQueue.get(0), device.device),
                    physicalDevice.graphicsQueueIndex)

                VulkanLogicalDevice(tracker, device, graphicsQueue, instance)
            }
        }
    }
}
