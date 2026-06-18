package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VkAllocationCallbacks

object VKInitializer {
    var vulkanInstance: VulkanInstance? = null

    fun exit() {
        vulkanInstance?.cleanupAsync()?.join()
    }

    fun init(tracker: ResourceTracker, extensions: Collection<String>) {
        val pAllocator: VkAllocationCallbacks? = null

        val instanceCreator = VKInitializerInstance()
        MemoryStack.stackPush().use { stack ->
            val vkInstance = instanceCreator.createInstance(stack, tracker, pAllocator, extensions)
            this.vulkanInstance = VulkanInstance(tracker, vkInstance)
        }
    }
}
