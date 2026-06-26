package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKInstance
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VkAllocationCallbacks

object VulkanInitializer {
    var vulkanInstance: VulkanInstance? = null

    fun exit() {
        vulkanInstance?.cleanupAsync()?.join()
    }

    fun init(tracker: ResourceTracker, extensions: Collection<String>) {
        val pAllocator: VkAllocationCallbacks? = null

        MemoryStack.stackPush().use { stack ->
            val vkInstance = VKInstance.createInstance(stack,
                VulkanDebug.enableValidationLayers,
                tracker,
                pAllocator,
                extensions)
            this.vulkanInstance = VulkanInstance(tracker, vkInstance)
        }
    }
}
