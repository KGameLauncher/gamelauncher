package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import de.dasbabypixel.gamelauncher.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkCommandBuffer
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo

class VKCommandBuffer(
    tracker: ResourceTracker, val device: VKDevice, val commandPool: VKCommandPool, val handle: VkCommandBuffer
) : AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit>? {
        VK10.vkFreeCommandBuffers(device.device, commandPool.handle, handle)
        return null
    }

    companion object {
        fun create(tracker: ResourceTracker, commandPool: VKCommandPool): VKCommandBuffer {
            return MemoryStack.stackPush().use { stack ->
                val device = commandPool.device
                val allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                    .`sType$Default`()
                    .commandPool(commandPool.handle)
                    .level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(1)

                val pCommandBuffers = stack.mallocPointer(1)
                VK10.vkAllocateCommandBuffers(device.device, allocInfo, pCommandBuffers).vkValidate()
                val commandBuffer = VkCommandBuffer(pCommandBuffers.get(0), device.device)

                VKCommandBuffer(tracker, device, commandPool, commandBuffer)
            }
        }
    }
}