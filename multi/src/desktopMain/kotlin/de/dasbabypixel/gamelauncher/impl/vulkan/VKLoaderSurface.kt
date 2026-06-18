package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKSurface
import org.lwjgl.system.MemoryStack

object VKLoaderSurface {
    fun loadForSurface(
        instance: VulkanInstance, tracker: ResourceTracker, surface: VKSurface
    ): LoadedDevice {
        val vkInstance = instance.instance
        MemoryStack.stackPush().use { stack ->
            val bestPhysicalDevice =
                VKDeviceSelection.selectBestPhysicalDevice(stack, vkInstance, surface)

            return VKLoaderDevice.loadDevice(tracker, bestPhysicalDevice, vkInstance)
        }
    }
}