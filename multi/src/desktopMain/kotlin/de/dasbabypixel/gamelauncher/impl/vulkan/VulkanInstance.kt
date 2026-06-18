package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKInstance
import org.lwjgl.vulkan.VkAllocationCallbacks

class VulkanInstance(tracker: ResourceTracker, val instance: VKInstance) :
    AbstractGameResource(tracker) {
    private var debugMessengerEnabled = false
    val pAllocator: VkAllocationCallbacks? = instance.pAllocator

    init {
        if (VKInitializerInstance.enableValidationLayers) {
            VKInitializerDebug.setupDebugMessenger(instance)
            debugMessengerEnabled = true
        }
    }

    override fun cleanup0(): CompletableFuture<Unit> {
        if (debugMessengerEnabled) {
            instance.destroyDebugMessenger()
        }
        return instance.cleanupAsync()
    }
}
