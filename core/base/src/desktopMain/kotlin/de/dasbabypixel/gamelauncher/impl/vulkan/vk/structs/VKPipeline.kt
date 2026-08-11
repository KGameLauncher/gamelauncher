package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import de.dasbabypixel.gamelauncher.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo

class VKPipeline(tracker: ResourceTracker, val handle: VkPipeline, val device: VKDevice) :
    AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit>? {
        VK10.vkDestroyPipeline(device.device, handle, device.pAllocator)
        return null
    }

    companion object {
        fun create(
            tracker: ResourceTracker, device: VKDevice, info: VkGraphicsPipelineCreateInfo
        ): VKPipeline {
            val info = VkGraphicsPipelineCreateInfo.create(info.address(), 1)
            return MemoryStack.stackPush().use { stack ->
                val pPipelines = stack.mallocLong(1)
                VK10.vkCreateGraphicsPipelines(device.device, 0L, info, device.pAllocator, pPipelines).vkValidate()
                val graphicsPipeline: VkPipeline = pPipelines.get(0)
                VKPipeline(tracker, graphicsPipeline, device)
            }
        }
    }
}