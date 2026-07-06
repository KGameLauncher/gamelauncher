package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.util.concurrent.allComplete
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKImageView

class VulkanImageViews(tracker: ResourceTracker, val swapChainImageViews: List<VKImageView>) :
    AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit> {
        return swapChainImageViews.map { it.cleanupAsync() }.allComplete()
    }

    companion object {
        fun create(tracker: ResourceTracker, vulkanSwapChain: VulkanSwapChain): VulkanImageViews {
            val views = vulkanSwapChain.images.map { image ->
                VKImageView.create(tracker,
                    vulkanSwapChain.surfaceFormat,
                    vulkanSwapChain.swapChain.device,
                    image.handle)
            }
            return VulkanImageViews(tracker, views)
        }
    }
}