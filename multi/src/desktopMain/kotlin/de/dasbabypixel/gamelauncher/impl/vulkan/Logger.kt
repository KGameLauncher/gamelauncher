package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.util.GameException

@JvmName("vkValidateExplicit")
internal fun vkValidate(res: Int) {
    if (res != 0) throw GameException("Error in vulkan")
}

@JvmName("vkValidate")
internal fun Int.vkValidate() = vkValidate(this)
