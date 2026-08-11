package de.dasbabypixel.gamelauncher.impl.window.glfw

import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.allComplete
import de.dasbabypixel.gamelauncher.api.util.concurrent.create
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanCommandBuffer
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanCommandPool
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanGraphicsPipeline
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanImageViews
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanLogicalDevice
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanSwapChain
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKFence
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKSemaphore
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VkResult
import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import de.dasbabypixel.gamelauncher.resource.ResourceTracker
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.ffm.FFM
import org.lwjgl.vulkan.KHRSwapchain
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkPresentInfoKHR
import org.lwjgl.vulkan.VkSubmitInfo
import java.lang.invoke.MethodHandles
import kotlin.concurrent.Volatile

class GLFWRenderThread(tracker: ResourceTracker, thread: Thread, val window: GLFWWindow) :
    AbstractExecutorThread(tracker, thread, customAwaitingSystem = true) {
    private val maxFramesInFlight: Int = 1
    private lateinit var swapChain: VulkanSwapChain
    private lateinit var device: VulkanLogicalDevice
    private lateinit var presentCompleteSemaphores: List<VKSemaphore>
    private lateinit var submitSemaphores: List<VKSemaphore>
    private lateinit var inFlightFences: List<VKFence>
    private lateinit var imageViews: VulkanImageViews
    private lateinit var graphicsPipeline: VulkanGraphicsPipeline
    private lateinit var commandPool: VulkanCommandPool
    private lateinit var commandBuffers: List<VulkanCommandBuffer>
    private var swapChainValid = false

    @Volatile
    private var framebufferResized = false
    private var frameIndex = 0
    fun framebufferResized() {
        framebufferResized = true
        signal()
    }

    override fun startExecuting() {
        device = window.surface.logicalDevice
        swapChain = VulkanSwapChain.create(tracker, window.surface, window.framebufferSize)
        imageViews = VulkanImageViews.create(tracker, swapChain)
        graphicsPipeline = VulkanGraphicsPipeline.create(tracker, device, swapChain.extent, swapChain.surfaceFormat)
        commandPool = VulkanCommandPool.create(tracker, device, graphicsPipeline)
        commandBuffers = List(maxFramesInFlight) {
            VulkanCommandBuffer.create(tracker, commandPool)
        }
        presentCompleteSemaphores = List(maxFramesInFlight) { VKSemaphore.create(tracker, device.device) }
        submitSemaphores = List(swapChain.images.size) { VKSemaphore.create(tracker, device.device) }
        inFlightFences = List(maxFramesInFlight) { VKFence.create(tracker, device.device, signaled = true) }

        swapChainValid = true

        signal()
    }

    override fun workExecution() {
//        Thread.sleep(1000)
        if (!swapChainValid) {
            recreateSwapChain()
        }
        drawFrame()
    }

    fun drawFrame() {
        if (!swapChainValid) {
            logger.debug("Skipping frame, swapChain invalid")
            return
        }
        println("Draw frame")
        MemoryStack.stackPush().use { stack ->

            val pImageIndex = stack.mallocInt(1)
            KHRSwapchain.vkAcquireNextImageKHR(device.device.device,
                swapChain.swapChain.handle,
                -1,
                presentCompleteSemaphores[frameIndex].handle,
                0L,
                pImageIndex).let { result: VkResult ->
                if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                    println("Out of date")
                    recreateSwapChain()
                    return
                } else if (result != KHRSwapchain.VK_SUBOPTIMAL_KHR) result.vkValidate()
            }
            inFlightFences[frameIndex].handle.let { fenceHandle ->
                VK10.vkResetFences(device.device.device, fenceHandle).vkValidate()
            }

            val imageIndex = pImageIndex.get(0)
            VK10.vkResetCommandBuffer(commandBuffers[frameIndex].commandBuffer.handle, 0).vkValidate()
            commandBuffers[frameIndex].recordCommandBuffer(swapChain, imageViews, imageIndex)

            val submitInfo = VkSubmitInfo.calloc(stack)
                .`sType$Default`()
                .waitSemaphoreCount(1)
                .pWaitSemaphores(stack.longs(presentCompleteSemaphores[frameIndex].handle))
                .pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
                .pCommandBuffers(stack.pointers(commandBuffers[frameIndex].commandBuffer.handle))
                .pSignalSemaphores(stack.longs(submitSemaphores[imageIndex].handle))

            VK10.vkQueueSubmit(device.graphicsQueue.queue, submitInfo, inFlightFences[frameIndex].handle).vkValidate()

            val presentInfo = VkPresentInfoKHR.calloc(stack)
                .`sType$Default`()
                .pWaitSemaphores(stack.longs(submitSemaphores[imageIndex].handle))
                .swapchainCount(1)
                .pSwapchains(stack.longs(swapChain.swapChain.handle))
                .pImageIndices(stack.ints(imageIndex))
            KHRSwapchain.vkQueuePresentKHR(device.graphicsQueue.queue, presentInfo).let { result: VkResult ->
                    if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR || framebufferResized) {
                        framebufferResized = false
                        println("Out of date or suboptimal")
                        recreateSwapChain()
                    } else result.vkValidate()
                }

            frameIndex = (frameIndex + 1) % maxFramesInFlight

            val fenceHandle = inFlightFences[frameIndex].handle
            VK10.vkWaitForFences(device.device.device, fenceHandle, true, -1).vkValidate()
        }
    }

    fun cleanupSwapChain() {
        if (!swapChainValid) return
        imageViews.cleanupAsync().resultNow()
        swapChain.cleanupAsync().resultNow()
        submitSemaphores.forEach { it.cleanupAsync().resultNow() }
    }

    fun recreateSwapChain() {
        if (swapChainValid) {
            device.waitIdle()

            cleanupSwapChain()
        }

        val framebufferSize = window.framebufferSize
        if (framebufferSize.x == 0 && framebufferSize.y == 0) {
            if (swapChainValid) {
                logger.debug("Framebuffer size is 0x0, pausing rendering")
                swapChainValid = false
            }
            return
        } else if (!swapChainValid) {
            logger.debug("Framebuffer size {}x{}, resuming rendering", framebufferSize.x, framebufferSize.y)
        }
        swapChain = VulkanSwapChain.create(tracker, window.surface, framebufferSize)
        imageViews = VulkanImageViews.create(tracker, swapChain)
        submitSemaphores = List(swapChain.images.size) { VKSemaphore.create(tracker, device.device) }
        swapChainValid = true

        FFM.ffmConfigBuilder(MethodHandles.lookup()).signal()
    }

    override fun stopExecuting() {
        device.waitIdle()

        val objects =
            inFlightFences + presentCompleteSemaphores + commandBuffers + listOf(commandPool, graphicsPipeline)
        objects.map { it.cleanupAsync() }.allComplete().resultNow()
        cleanupSwapChain()
    }

    override fun preLoop() {
        workSignal.await()
    }

    override fun postLoop() {
    }

    override fun customAwaitWork() = workSignal.await()
    override fun customSignal() = workSignal.signal()

    companion object {
        private val logger by getLogger()
        fun create(tracker: ResourceTracker, window: GLFWWindow): GLFWRenderThread {
            return Thread.create("RenderThread-" + window.identifier,
                { thread -> GLFWRenderThread(tracker, thread, window) })
        }
    }
}
