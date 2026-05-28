package de.dasbabypixel.gamelauncher.lwjgl.vulkan.vk.structs

import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK11
import org.lwjgl.vulkan.VK12
import org.lwjgl.vulkan.VK13
import org.lwjgl.vulkan.VK14

enum class VKApiVersion(val vk: Int) {
    V10(VK10.VK_API_VERSION_1_0),
    V11(VK11.VK_API_VERSION_1_1),
    V12(VK12.VK_API_VERSION_1_2),
    V13(VK13.VK_API_VERSION_1_3),
    V14(VK14.VK_API_VERSION_1_4)
}
