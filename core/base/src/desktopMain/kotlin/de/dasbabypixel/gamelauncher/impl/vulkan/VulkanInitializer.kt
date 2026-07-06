package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKInstance
import de.dasbabypixel.gamelauncher.logging.LoggingInstance
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VkAllocationCallbacks

object VulkanInitializer {
    var vulkanInstance: VulkanInstance? = null

    fun exit() {
        vulkanInstance?.cleanupAsync()?.join()
    }

    fun init(
        tracker: ResourceTracker, extensions: Collection<String>, loggingInstance: LoggingInstance
    ) {
        val pAllocator: VkAllocationCallbacks? = null

        val debug = VulkanDebug(loggingInstance)
        MemoryStack.stackPush().use { stack ->
            val vkInstance = VKInstance.createInstance(
                stack,
                debug.enableValidationLayers,
                tracker,
                pAllocator,
                extensions,
                loggingInstance,
                debug
            )
            this.vulkanInstance = VulkanInstance(tracker, vkInstance)
        }
    }
}
