package de.dasbabypixel.gamelauncher.api.lifecycle

import de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j.LWJGLLogging
import de.dasbabypixel.gamelauncher.impl.vulkan.VKInitializer
import de.dasbabypixel.gamelauncher.impl.window.WindowSystems

actual fun ShutdownHandler.shutdownGracefullyPlatform() {
    WindowSystems.terminate()?.join()
    VKInitializer.exit()
    LWJGLLogging.exit()
}