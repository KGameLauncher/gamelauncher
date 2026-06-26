package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKCommandPool
import org.lwjgl.system.MemoryStack

class VulkanCommandPool(
    tracker: ResourceTracker,
    val commandPool: VKCommandPool,
    val graphicsPipeline: VulkanGraphicsPipeline
) : AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit> {
        return commandPool.cleanupAsync()
    }

    companion object {
        fun create(
            tracker: ResourceTracker,
            device: VulkanLogicalDevice,
            graphicsPipeline: VulkanGraphicsPipeline
        ): VulkanCommandPool {
            return MemoryStack.stackPush().use { stack ->
                VulkanCommandPool(tracker,
                    VKCommandPool.create(tracker, device.device, device.graphicsQueue),
                    graphicsPipeline)
            }
        }
    }
}
