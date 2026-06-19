package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKCommandPool
import org.lwjgl.system.MemoryStack

class VulkanCommandPool(
    tracker: ResourceTracker,
    val swapChain: VulkanSwapChain,
    val commandPool: VKCommandPool,
    val graphicsPipeline: VulkanGraphicsPipeline
) : AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit> {
        return commandPool.cleanupAsync()
    }

    companion object {
        fun create(
            tracker: ResourceTracker,
            swapChain: VulkanSwapChain,
            graphicsPipeline: VulkanGraphicsPipeline
        ): VulkanCommandPool {
            return MemoryStack.stackPush().use { stack ->
                VulkanCommandPool(tracker,
                    swapChain,
                    VKCommandPool.create(tracker,
                        swapChain.device.device,
                        swapChain.device.graphicsQueue),
                    graphicsPipeline)
            }
        }
    }
}
