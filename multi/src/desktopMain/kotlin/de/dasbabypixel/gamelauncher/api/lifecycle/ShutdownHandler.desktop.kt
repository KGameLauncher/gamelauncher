package de.dasbabypixel.gamelauncher.api.lifecycle

import de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j.LWJGLLogging
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanInitializer
import de.dasbabypixel.gamelauncher.impl.window.WindowSystems

actual fun ShutdownHandler.shutdownGracefullyPlatform() {
    WindowSystems.terminate()?.join()
    VulkanInitializer.exit()
    LWJGLLogging.exit()
}