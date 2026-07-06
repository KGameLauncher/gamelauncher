package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKSurface
import org.lwjgl.system.MemoryStack

class VulkanSurface(
    tracker: ResourceTracker,
    val surface: VKSurface,
    val logicalDevice: VulkanLogicalDevice,
    val instance: VulkanInstance
) : AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit> {
        return surface.cleanupAsync().thenCompose { logicalDevice.cleanupAsync() }
    }

    companion object {
        fun create(
            tracker: ResourceTracker, handle: Long, instance: VulkanInstance
        ): VulkanSurface {
            val surface = VKSurface(tracker, handle, instance.instance)
            val logicalDevice = MemoryStack.stackPush().use { stack ->
                val bestPhysicalDevice = instance.deviceSelection.selectBestPhysicalDevice(
                    stack,
                    instance.instance,
                    surface
                )

                VulkanLogicalDevice.create(tracker, bestPhysicalDevice, instance)
            }

            return VulkanSurface(tracker, surface, logicalDevice, instance)
        }
    }
}