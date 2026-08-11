package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import de.dasbabypixel.gamelauncher.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.resource.ResourceTracker
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkSemaphoreCreateInfo

class VKSemaphore(tracker: ResourceTracker, val handle: VkSemaphore, val device: VKDevice) :
    AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit>? {
        VK10.vkDestroySemaphore(device.device, handle, device.pAllocator)
        return null
    }

    companion object {
        fun create(tracker: ResourceTracker, device: VKDevice): VKSemaphore {
            return MemoryStack.stackPush().use { stack ->
                val createInfo = VkSemaphoreCreateInfo.calloc(stack).`sType$Default`()
                val pSemaphore = stack.mallocLong(1)
                VK10.vkCreateSemaphore(device.device, createInfo, device.pAllocator, pSemaphore).vkValidate()
                val semaphore: VkSemaphore = pSemaphore.get(0)
                VKSemaphore(tracker, semaphore, device)
            }
        }
    }
}
