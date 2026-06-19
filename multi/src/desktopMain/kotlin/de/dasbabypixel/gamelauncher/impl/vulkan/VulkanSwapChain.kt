package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.math.Vec2i
import de.dasbabypixel.gamelauncher.api.math.clamp
import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKSurfaceFormatKHR
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKSwapChain
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VkImage
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VkSurfaceTransformFlagBitsKHR
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.KHRSurface
import org.lwjgl.vulkan.KHRSwapchain
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkExtent2D
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR
import org.lwjgl.vulkan.VkSurfaceFormatKHR
import java.nio.IntBuffer
import kotlin.math.max

class VulkanSwapChain(
    tracker: ResourceTracker,
    val swapChain: VKSwapChain,
    val surface: VulkanSurface,
    val device: VulkanLogicalDevice
) : AbstractGameResource(tracker) {
    val images: List<Image>
    val extent: Vec2i
    val surfaceFormat: VKSurfaceFormatKHR

    init {
        MemoryStack.stackPush().use { stack ->
            val pCount = stack.mallocInt(1)
            KHRSwapchain.vkGetSwapchainImagesKHR(swapChain.device.device,
                swapChain.handle,
                pCount,
                null).vkValidate()
            val pSwapChainImages = stack.mallocLong(pCount.get(0))
            KHRSwapchain.vkGetSwapchainImagesKHR(swapChain.device.device,
                swapChain.handle,
                pCount,
                pSwapChainImages).vkValidate()

            images = List(pSwapChainImages.capacity()) { Image(pSwapChainImages.get(it)) }
            extent = swapChain.extent
            surfaceFormat = swapChain.surfaceFormat
        }
    }

    override fun cleanup0(): CompletableFuture<Unit> {
        return swapChain.cleanupAsync()
    }

    class Image(val handle: VkImage)

    companion object {
        fun create(
            tracker: ResourceTracker, vulkanSurface: VulkanSurface, framebufferSize: Vec2i
        ): VulkanSwapChain {
            val device = vulkanSurface.logicalDevice.device
            val surface = vulkanSurface.surface
            return MemoryStack.stackPush().use { stack ->
                val surfaceCapabilities = VkSurfaceCapabilitiesKHR.malloc(stack)
                surface.surfaceCapabilities(device, surfaceCapabilities)

                val extent = chooseSwapExtent(stack,
                    surfaceCapabilities,
                    framebufferSize).let { Vec2i(it.width(), it.height()) }
                val minImageCount = chooseSwapMinImageCount(surfaceCapabilities)
                val availableSurfaceFormats = surface.availableFormats(device, stack) { it }
                val surfaceFormat = chooseSwapSurfaceFormat(availableSurfaceFormats).let {
                    VKSurfaceFormatKHR(it.format(), it.colorSpace())
                }
                val presentMode =
                    surface.availablePresentModes(device) { chooseSwapPresentMode(it) }
                val preTransform: VkSurfaceTransformFlagBitsKHR =
                    surfaceCapabilities.currentTransform()

                val swapChain = VKSwapChain.create(tracker,
                    device,
                    surface,
                    minImageCount,
                    extent,
                    surfaceFormat,
                    preTransform,
                    presentMode)

                VulkanSwapChain(tracker, swapChain, vulkanSurface, vulkanSurface.logicalDevice)
            }
        }

        private fun chooseSwapMinImageCount(surfaceCapabilities: VkSurfaceCapabilitiesKHR): Int {
            val minImageCount = max(surfaceCapabilities.minImageCount(), 3)
            if (surfaceCapabilities.maxImageCount() in 1..<minImageCount) {
                return surfaceCapabilities.maxImageCount()
            }
            return minImageCount
        }

        private fun chooseSwapSurfaceFormat(surfaceFormats: VkSurfaceFormatKHR.Buffer): VkSurfaceFormatKHR {
            if (surfaceFormats.capacity() == 0) throw GameException("No surface formats available")
            surfaceFormats.filter {
                it.format() == VK10.VK_FORMAT_B8G8R8A8_SRGB
            }.firstOrNull {
                it.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR
            }?.let { return it }
            return surfaceFormats.get(0)
        }

        private fun chooseSwapPresentMode(presentModes: IntBuffer): Int {
            val modes = IntArray(presentModes.capacity())
            for (i in 0..<presentModes.capacity()) {
                modes[i] = presentModes.get(i)
            }
            if (modes.contains(KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR)) {
                return KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR
            }
            if (!modes.contains(KHRSurface.VK_PRESENT_MODE_FIFO_KHR)) throw GameException("FIFO present mode not supported")
            return KHRSurface.VK_PRESENT_MODE_FIFO_KHR
        }

        private fun chooseSwapExtent(
            stack: MemoryStack,
            surfaceCapabilities: VkSurfaceCapabilitiesKHR,
            framebufferSize: Vec2i
        ): VkExtent2D {
            val currentExtent = surfaceCapabilities.currentExtent()
            if (currentExtent.width() != -1) {
                return currentExtent
            }
            val minExtent = surfaceCapabilities.minImageExtent()
            val maxExtent = surfaceCapabilities.maxImageExtent()
            return VkExtent2D.malloc(stack)
                .width(framebufferSize.x.clamp(minExtent.width(), maxExtent.width()))
                .height(framebufferSize.y.clamp(minExtent.height(), maxExtent.height()))
        }
    }
}