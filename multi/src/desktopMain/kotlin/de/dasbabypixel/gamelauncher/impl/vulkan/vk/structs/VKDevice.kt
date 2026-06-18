package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkAllocationCallbacks
import org.lwjgl.vulkan.VkDevice

class VKDevice(
    tracker: ResourceTracker,
    val instance: VKInstance,
    val device: VkDevice,
    val pAllocator: VkAllocationCallbacks?
) : AbstractGameResource(tracker) {
    init {
        instance.devices.add(this)
    }

    override fun cleanup0(): CompletableFuture<Unit>? {
        VK10.vkDestroyDevice(device, pAllocator)
        instance.devices.remove(this)
        return null
    }
}
