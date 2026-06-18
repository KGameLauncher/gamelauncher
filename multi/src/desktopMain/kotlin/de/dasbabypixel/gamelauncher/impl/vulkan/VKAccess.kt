package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKInstance

object VKAccess {
    val instance: VKInstance
        get() = VKInitializer.vkInstance!!
}