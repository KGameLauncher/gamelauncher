package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKInstance
import org.lwjgl.system.MemoryStack

class VulkanInstance(tracker: ResourceTracker, val instance: VKInstance) :
    AbstractGameResource(tracker) {
    val deviceSelection = VulkanDeviceSelection(instance)

    init {
        if (instance.validationLayersEnabled) {
            instance.setupDebugMessenger()
        }
    }

    fun createSurface(surfaceIdCreator: (MemoryStack, VKInstance) -> Long): VulkanSurface {
        val id = MemoryStack.stackPush().use { stack ->
            surfaceIdCreator(stack, instance)
        }
        return VulkanSurface.create(tracker, id, this)
    }

    override fun cleanup0(): CompletableFuture<Unit> {
        if (instance.validationLayersEnabled) {
            instance.destroyDebugMessenger()
        }
        return instance.cleanupAsync()
    }
}
