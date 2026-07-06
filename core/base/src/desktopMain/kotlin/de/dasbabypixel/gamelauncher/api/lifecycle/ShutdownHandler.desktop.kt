package de.dasbabypixel.gamelauncher.api.lifecycle

import de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j.LWJGLLogging
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanInitializer
import de.dasbabypixel.gamelauncher.impl.window.WindowSystems

actual fun ShutdownHandler.shutdownGracefullyPlatform() {
    println("Begin shutdown window systems")
    gameLauncher.serviceRegistry.singleInstance<WindowSystems>().terminate()?.join()
    println("Begin shutdown vulkan")
    VulkanInitializer.exit()
    println("Begin shutdown logging")
    gameLauncher.serviceRegistry.singleInstance<LWJGLLogging>().exit()
}
