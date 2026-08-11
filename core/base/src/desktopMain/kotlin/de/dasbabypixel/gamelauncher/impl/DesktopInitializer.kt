package de.dasbabypixel.gamelauncher.impl

import de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j.LWJGLLogging
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanInitializer
import de.dasbabypixel.gamelauncher.impl.window.WindowSystems
import de.dasbabypixel.gamelauncher.logging.getLogger
import de.dasbabypixel.gamelauncher.resource.SimpleResourceTracker

object DesktopInitializer {
    private val logger by getLogger()
    fun init(thread: StartupThread) {


        LWJGLLogging.startReader()

        initWindowAndRendering(thread)
    }

    private fun initWindowAndRendering(thread: StartupThread) {
        WindowSystems.load()
        val windowSystem = WindowSystems.selected()
        thread.selectedWindowSystem.complete(windowSystem)

        VulkanInitializer.init(SimpleResourceTracker.global, windowSystem.getVulkanExtensions())

        val window = windowSystem.createWindow().build().join()
        window.show().join()
    }
}
