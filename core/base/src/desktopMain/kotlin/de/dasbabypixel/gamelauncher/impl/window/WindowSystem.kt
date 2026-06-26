package de.dasbabypixel.gamelauncher.impl.window

import de.dasbabypixel.gamelauncher.api.resource.GameResource

interface WindowSystem : GameResource {
    fun createWindow(): WindowBuilder
    fun init()
    fun getVulkanExtensions(): Collection<String>
    fun takeOverInitialThread()
}