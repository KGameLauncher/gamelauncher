package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.KHRSurface
import org.lwjgl.vulkan.KHRSwapchain
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkExtent2D
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR

class VKSwapChain(
    val device: VKDevice,
    val handle: Long,
    val extent: VKExtent2D,
    val surfaceFormat: VKSurfaceFormatKHR,
    tracker: ResourceTracker
) : AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit>? {
        KHRSwapchain.vkDestroySwapchainKHR(device.device, handle, device.pAllocator)
        return null
    }

    companion object {
        fun create(
            tracker: ResourceTracker,
            device: VKDevice,
            surface: VKSurface,
            minImageCount: Int,
            extent: VKExtent2D,
            surfaceFormat: VKSurfaceFormatKHR,
            preTransform: VkSurfaceTransformFlagBitsKHR,
            presentMode: VkPresentModeKHR
        ): VKSwapChain {
            return MemoryStack.stackPush().use { stack ->
                val swapChainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                    .`sType$Default`()
                    .surface(surface.handle)
                    .minImageCount(minImageCount)
                    .imageFormat(surfaceFormat.format)
                    .imageColorSpace(surfaceFormat.colorSpace)
                    .imageExtent(VkExtent2D.malloc(stack).width(extent.x).height(extent.y))
                    .imageArrayLayers(1)
                    .imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                    .imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
                    .preTransform(preTransform)
                    .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                    .presentMode(presentMode)
                    .clipped(true)
                    .oldSwapchain(0L) // TODO

                val pSwapChain = stack.mallocLong(1)
                KHRSwapchain.vkCreateSwapchainKHR(device.device,
                    swapChainCreateInfo,
                    device.pAllocator,
                    pSwapChain).vkValidate()
                val handle = pSwapChain.get(0)
                VKSwapChain(device, handle, extent, surfaceFormat, tracker)
            }
        }
    }
}