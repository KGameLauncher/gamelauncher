package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import de.dasbabypixel.gamelauncher.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkShaderModuleCreateInfo

class VKShaderModule(tracker: ResourceTracker, val handle: VkShaderModule, val device: VKDevice) :
    AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit>? {
        VK10.vkDestroyShaderModule(device.device, handle, device.pAllocator)
        return null
    }

    companion object {
        fun create(tracker: ResourceTracker, device: VKDevice, code: ByteArray): VKShaderModule {
            return MemoryStack.stackPush().use { stack ->
                val pShaderModule = stack.mallocLong(1)
                val pCreateInfo = VkShaderModuleCreateInfo.calloc(stack).`sType$Default`().pCode(stack.bytes(*code))
                VK10.vkCreateShaderModule(device.device, pCreateInfo, device.pAllocator, pShaderModule).vkValidate()
                VKShaderModule(tracker, pShaderModule.get(0), device)
            }
        }
    }
}
