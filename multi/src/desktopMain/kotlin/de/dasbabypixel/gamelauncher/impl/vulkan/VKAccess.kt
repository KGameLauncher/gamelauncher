package de.dasbabypixel.gamelauncher.impl.vulkan

object VKAccess {
    val instance: VulkanInstance
        get() = VKInitializer.vulkanInstance!!
}
