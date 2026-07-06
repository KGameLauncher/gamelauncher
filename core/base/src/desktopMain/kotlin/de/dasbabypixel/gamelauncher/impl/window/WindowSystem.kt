package de.dasbabypixel.gamelauncher.impl.window

import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.util.resource.GameResource

interface WindowSystem : GameResource {
    fun createWindow(): WindowBuilder
    fun init()
    fun getVulkanExtensions(): Collection<String>
    fun takeOverInitialThread()

    fun cleanupAsync(): CompletableFuture<Unit>
}