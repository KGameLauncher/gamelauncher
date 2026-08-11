package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import de.dasbabypixel.gamelauncher.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkFenceCreateInfo

class VKFence(tracker: ResourceTracker, val handle: VkFence, val device: VKDevice) : AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit>? {
        VK10.vkDestroyFence(device.device, handle, device.pAllocator)
        return null
    }

    companion object {
        fun create(tracker: ResourceTracker, device: VKDevice, signaled: Boolean = false): VKFence {
            return MemoryStack.stackPush().use { stack ->
                val createInfo = VkFenceCreateInfo.calloc(stack).`sType$Default`().also {
                    if (signaled) {
                        it.flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT)
                    }
                }
                val pFence = stack.mallocLong(1)
                VK10.vkCreateFence(device.device, createInfo, device.pAllocator, pFence).vkValidate()
                val fence: VkFence = pFence.get(0)
                VKFence(tracker, fence, device)
            }
        }
    }
}
