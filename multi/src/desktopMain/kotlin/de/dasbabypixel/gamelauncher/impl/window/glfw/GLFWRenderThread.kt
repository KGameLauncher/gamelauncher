package de.dasbabypixel.gamelauncher.impl.window.glfw

import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.api.util.concurrent.allComplete
import de.dasbabypixel.gamelauncher.api.util.concurrent.create
import de.dasbabypixel.gamelauncher.api.util.concurrent.sleep
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanCommandBuffer
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanCommandPool
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanGraphicsPipeline
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanImageViews
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanLogicalDevice
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanSwapChain
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKFence
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKSemaphore
import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.KHRSwapchain
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkPresentInfoKHR
import org.lwjgl.vulkan.VkSubmitInfo

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
    private var frameIndex = 0

    override fun startExecuting() {
        swapChain = VulkanSwapChain.create(tracker, window.surface, window.framebufferSize)
        device = swapChain.device
        imageViews = VulkanImageViews.create(tracker, swapChain)
        graphicsPipeline = VulkanGraphicsPipeline.create(tracker, swapChain)
        commandPool = VulkanCommandPool.create(tracker, swapChain, graphicsPipeline)
        commandBuffers =
            List(maxFramesInFlight) { VulkanCommandBuffer.create(tracker, commandPool, imageViews) }
        presentCompleteSemaphores =
            List(maxFramesInFlight) { VKSemaphore.create(tracker, device.device) }
        submitSemaphores =
            List(swapChain.images.size) { VKSemaphore.create(tracker, device.device) }
        inFlightFences =
            List(maxFramesInFlight) { VKFence.create(tracker, device.device, signaled = true) }
    }

    override fun workExecution() {
        Thread.sleep(500)
        drawFrame()
    }

    fun drawFrame() {
        MemoryStack.stackPush().use { stack ->

            val fenceHandle = inFlightFences[frameIndex].handle
            VK10.vkWaitForFences(device.device.device, fenceHandle, true, -1).vkValidate()
            VK10.vkResetFences(device.device.device, fenceHandle).vkValidate()

            val pImageIndex = stack.mallocInt(1)
            KHRSwapchain.vkAcquireNextImageKHR(device.device.device,
                swapChain.swapChain.handle,
                -1,
                presentCompleteSemaphores[frameIndex].handle,
                0L,
                pImageIndex).vkValidate()
            val imageIndex = pImageIndex.get(0)
            VK10.vkResetCommandBuffer(commandBuffers[frameIndex].commandBuffer.handle, 0)
                .vkValidate()
            commandBuffers[frameIndex].recordCommandBuffer(imageIndex)

            val submitInfo = VkSubmitInfo.calloc(stack)
                .`sType$Default`()
                .waitSemaphoreCount(1)
                .pWaitSemaphores(stack.longs(presentCompleteSemaphores[frameIndex].handle))
                .pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
                .pCommandBuffers(stack.pointers(commandBuffers[frameIndex].commandBuffer.handle))
                .pSignalSemaphores(stack.longs(submitSemaphores[imageIndex].handle))

            VK10.vkQueueSubmit(device.graphicsQueue.queue,
                submitInfo,
                inFlightFences[frameIndex].handle).vkValidate()

            val presentInfo = VkPresentInfoKHR.calloc(stack)
                .`sType$Default`()
                .pWaitSemaphores(stack.longs(submitSemaphores[imageIndex].handle))
                .swapchainCount(1)
                .pSwapchains(stack.longs(swapChain.swapChain.handle))
                .pImageIndices(stack.ints(imageIndex))
            KHRSwapchain.vkQueuePresentKHR(device.graphicsQueue.queue, presentInfo).vkValidate()

            frameIndex = (frameIndex + 1) % maxFramesInFlight
        }
    }

    override fun stopExecuting() {
        VK10.vkDeviceWaitIdle(device.device.device).vkValidate()
        val objects =
            inFlightFences + submitSemaphores + presentCompleteSemaphores + commandBuffers + listOf(
                commandPool,
                graphicsPipeline,
                imageViews,
                swapChain)
        objects.map { it.cleanupAsync() }.allComplete().join()
    }

    override fun preLoop() {
    }

    override fun postLoop() {
    }

    override fun customAwaitWork() = error("Should not be called")


    companion object {
        fun create(tracker: ResourceTracker, window: GLFWWindow): GLFWRenderThread {
            return Thread.create("RenderThread-" + window.identifier,
                { thread -> GLFWRenderThread(tracker, thread, window) })
        }
    }
}
