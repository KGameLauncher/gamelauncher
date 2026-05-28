package de.dasbabypixel.gamelauncher.lwjgl.vulkan.vk.structs

import org.lwjgl.vulkan.VK10

data class VKVersion(val major: Int, val minor: Int, val patch: Int) {
    val vkVersion: Int
        get() = VK10.VK_MAKE_VERSION(major, minor, patch)
}
