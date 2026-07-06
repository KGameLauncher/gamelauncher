package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkComponentMapping
import org.lwjgl.vulkan.VkImageSubresourceRange
import org.lwjgl.vulkan.VkImageViewCreateInfo

class VKImageView(tracker: ResourceTracker, val handle: VkImageView, val device: VKDevice) :
    AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit>? {
        VK10.vkDestroyImageView(device.device, handle, device.pAllocator)
        return null
    }

    companion object {
        fun create(
            tracker: ResourceTracker,
            surfaceFormat: VKSurfaceFormatKHR,
            device: VKDevice,
            image: VkImage
        ): VKImageView {
            return MemoryStack.stackPush().use { stack ->
                val createInfo = VkImageViewCreateInfo.calloc(stack)
                    .`sType$Default`()
                    .viewType(VK10.VK_IMAGE_VIEW_TYPE_2D)
                    .format(surfaceFormat.format)
                    .subresourceRange(VkImageSubresourceRange.calloc()
                        .set(VK10.VK_IMAGE_ASPECT_COLOR_BIT, 0, 1, 0, 1))
                    .components(VkComponentMapping.calloc(stack)
                        .set(VK10.VK_COMPONENT_SWIZZLE_IDENTITY,
                            VK10.VK_COMPONENT_SWIZZLE_IDENTITY,
                            VK10.VK_COMPONENT_SWIZZLE_IDENTITY,
                            VK10.VK_COMPONENT_SWIZZLE_IDENTITY))
                    .image(image)
                val pView = stack.mallocLong(1)
                VK10.vkCreateImageView(device.device, createInfo, device.pAllocator, pView)
                    .vkValidate()
                val handle = pView.get(0)

                VKImageView(tracker, handle, device)
            }
        }
    }
}