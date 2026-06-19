package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.util.GameException
import org.lwjgl.vulkan.VK10

@JvmName("vkValidateExplicit")
internal fun vkValidate(res: Int) {
    if (res != VK10.VK_SUCCESS) throw GameException("Error in vulkan: ${res.toHexString()}")
    
}

@JvmName("vkValidate")
internal fun Int.vkValidate() = vkValidate(this)
