package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import de.dasbabypixel.gamelauncher.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.resource.ResourceTracker
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkCommandPoolCreateInfo

class VKCommandPool(
    tracker: ResourceTracker, val handle: VkCommandPool, val device: VKDevice
) : AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit>? {
        VK10.vkDestroyCommandPool(device.device, handle, device.pAllocator)
        return null
    }

    companion object {
        fun create(tracker: ResourceTracker, device: VKDevice, queue: VKQueue): VKCommandPool {
            return MemoryStack.stackPush().use { stack ->

                val poolInfo = VkCommandPoolCreateInfo.calloc(stack)
                    .`sType$Default`()
                    .flags(VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                    .queueFamilyIndex(queue.queueFamilyIndex)

                val pCommandPool = stack.mallocLong(1)
                VK10.vkCreateCommandPool(device.device, poolInfo, device.pAllocator, pCommandPool).vkValidate()
                val commandPool: VkCommandPool = pCommandPool.get(0)
                VKCommandPool(tracker, commandPool, device)
            }
        }
    }
}
