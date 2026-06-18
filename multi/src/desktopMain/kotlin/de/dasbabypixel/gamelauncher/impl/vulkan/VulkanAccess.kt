package de.dasbabypixel.gamelauncher.impl.vulkan

object VulkanAccess {
    val instance: VulkanInstance
        get() = VulkanInitializer.vulkanInstance!!
}
