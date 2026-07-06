package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo

class VKPipelineLayout(
    tracker: ResourceTracker, val handle: VkPipelineLayout, val device: VKDevice
) : AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit>? {
        VK10.vkDestroyPipelineLayout(device.device, handle, device.pAllocator)
        return null
    }

    companion object {
        fun create(tracker: ResourceTracker, device: VKDevice): VKPipelineLayout {
            return MemoryStack.stackPush().use { stack ->
                val pipelineLayoutInfo =
                    VkPipelineLayoutCreateInfo.calloc(stack).`sType$Default`().setLayoutCount(0)

                val pPipelineLayout = stack.mallocLong(1)
                VK10.vkCreatePipelineLayout(device.device,
                    pipelineLayoutInfo,
                    device.pAllocator,
                    pPipelineLayout).vkValidate()
                val pipelineLayout: VkPipelineLayout = pPipelineLayout.get(0)
                VKPipelineLayout(tracker, pipelineLayout, device)
            }
        }
    }
}