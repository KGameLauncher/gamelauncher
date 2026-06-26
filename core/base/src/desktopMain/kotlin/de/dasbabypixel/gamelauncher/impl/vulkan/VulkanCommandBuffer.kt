package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKCommandBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.KHRSwapchain
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK13
import org.lwjgl.vulkan.VkClearColorValue
import org.lwjgl.vulkan.VkClearValue
import org.lwjgl.vulkan.VkCommandBufferBeginInfo
import org.lwjgl.vulkan.VkDependencyInfo
import org.lwjgl.vulkan.VkImageMemoryBarrier2
import org.lwjgl.vulkan.VkImageSubresourceRange
import org.lwjgl.vulkan.VkRect2D
import org.lwjgl.vulkan.VkRenderingAttachmentInfo
import org.lwjgl.vulkan.VkRenderingInfo
import org.lwjgl.vulkan.VkViewport

class VulkanCommandBuffer(
    tracker: ResourceTracker, val commandBuffer: VKCommandBuffer, val commandPool: VulkanCommandPool
) : AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit> {
        return commandBuffer.cleanupAsync()
    }

    fun recordCommandBuffer(
        swapChain: VulkanSwapChain, imageViews: VulkanImageViews, imageIndex: Int
    ) {
        MemoryStack.stackPush().use { stack ->
            VK10.vkBeginCommandBuffer(commandBuffer.handle,
                VkCommandBufferBeginInfo.calloc(stack).`sType$Default`())
            val swapChainExtent = swapChain.extent

            transitionImageLayout(swapChain,
                imageIndex,
                VK10.VK_IMAGE_LAYOUT_UNDEFINED,
                VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                0L,
                VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
                VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
                VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)

            val clearColor = VkClearValue.calloc(stack)
                .color(VkClearColorValue.calloc(stack)
                    .float32(0, 0F)
                    .float32(1, 0F)
                    .float32(2, 0F)
                    .float32(3, 1F))
            val attachmentInfo = VkRenderingAttachmentInfo.calloc(1, stack)
                .`sType$Default`()
                .imageView(imageViews.swapChainImageViews[imageIndex].handle)
                .imageLayout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                .loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
                .clearValue(clearColor)

            val renderingInfo = VkRenderingInfo.calloc(stack).`sType$Default`().renderArea { area ->
                area.offset { it.set(0, 0) }
                area.extent { it.set(swapChainExtent.x, swapChainExtent.y) }
            }.layerCount(1).pColorAttachments(attachmentInfo)

            VK13.vkCmdBeginRendering(commandBuffer.handle, renderingInfo)

            VK10.vkCmdBindPipeline(commandBuffer.handle,
                VK10.VK_PIPELINE_BIND_POINT_GRAPHICS,
                commandPool.graphicsPipeline.pipeline.handle)

            VK10.vkCmdSetViewport(commandBuffer.handle,
                0,
                VkViewport.calloc(1, stack)
                    .x(0F)
                    .y(0F)
                    .width(swapChainExtent.x.toFloat())
                    .height(swapChainExtent.y.toFloat())
                    .minDepth(0F)
                    .maxDepth(1F))

            VK10.vkCmdSetScissor(commandBuffer.handle,
                0,
                VkRect2D.calloc(1, stack)
                    .offset { it.set(0, 0) }
                    .extent { it.set(swapChainExtent.x, swapChainExtent.y) })

            VK10.vkCmdDraw(commandBuffer.handle, 3, 1, 0, 0)

            VK13.vkCmdEndRendering(commandBuffer.handle)

            transitionImageLayout(swapChain,
                imageIndex,
                VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
                VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
                0,
                VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
                VK13.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT)

            VK10.vkEndCommandBuffer(commandBuffer.handle).vkValidate()
        }
    }

    fun transitionImageLayout(
        swapChain: VulkanSwapChain,
        imageIndex: Int,
        oldLayout: Int,
        newLayout: Int,
        srcAccessMask: Long,
        dstAccessMask: Long,
        srcStageMask: Long,
        dstStageMask: Long
    ) {
        MemoryStack.stackPush().use { stack ->
            val barrier = VkImageMemoryBarrier2.calloc(1, stack)
                .`sType$Default`()
                .srcStageMask(srcStageMask)
                .dstStageMask(dstStageMask)
                .srcAccessMask(srcAccessMask)
                .dstAccessMask(dstAccessMask)
                .oldLayout(oldLayout)
                .newLayout(newLayout)
                .srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
                .dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
                .image(swapChain.images[imageIndex].handle)
                .subresourceRange(VkImageSubresourceRange.calloc(stack)
                    .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1))
            val dependencyInfo = VkDependencyInfo.calloc(stack)
                .`sType$Default`()
                .dependencyFlags(0)
                .pImageMemoryBarriers(barrier)

            VK13.vkCmdPipelineBarrier2(commandBuffer.handle, dependencyInfo)
        }
    }

    companion object {
        fun create(
            tracker: ResourceTracker, commandPool: VulkanCommandPool
        ): VulkanCommandBuffer {
            return MemoryStack.stackPush().use { stack ->
                val commandBuffer = VKCommandBuffer.create(tracker, commandPool.commandPool)
                VulkanCommandBuffer(tracker, commandBuffer, commandPool)
            }
        }
    }
}