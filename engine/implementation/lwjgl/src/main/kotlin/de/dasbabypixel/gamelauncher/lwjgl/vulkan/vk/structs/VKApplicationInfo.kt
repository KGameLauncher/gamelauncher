package de.dasbabypixel.gamelauncher.lwjgl.vulkan.vk.structs

data class VKApplicationInfo(
    val applicationName: String,
    val applicationVersion: VKVersion,
    val engineName: String,
    val engineVersion: VKVersion,
    val apiVersion: VKApiVersion
)
