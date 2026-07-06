package de.dasbabypixel.gamelauncher.impl

import de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j.LWJGLLogging
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanInitializer
import de.dasbabypixel.gamelauncher.impl.window.WindowSystems
import de.dasbabypixel.gamelauncher.logging.LoggingInstance
import de.dasbabypixel.gamelauncher.logging.getLogger
import de.dasbabypixel.gamelauncher.service.ServiceRegistry
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker

class DesktopInitializer(val serviceRegistry: ServiceRegistry) {
    private val logger by getLogger(serviceRegistry.singleInstance())
    fun init(thread: StartupThread) {
        serviceRegistry.singleInstance<LWJGLLogging>().startReader()

        initWindowAndRendering(thread)
    }

    private fun initWindowAndRendering(thread: StartupThread) {
        val tracker = serviceRegistry.singleInstance<ResourceTracker>()
        val loggingInstance = serviceRegistry.singleInstance<LoggingInstance>()
        serviceRegistry.register { WindowSystems(tracker, loggingInstance, serviceRegistry) }
        val windowSystems = serviceRegistry.singleInstance<WindowSystems>()
        windowSystems.load()
        val windowSystem = windowSystems.selected()
        thread.selectedWindowSystem.complete(windowSystem)

        VulkanInitializer.init(
            serviceRegistry.singleInstance(),
            windowSystem.getVulkanExtensions(),
            serviceRegistry.singleInstance()
        )

        val window = windowSystem.createWindow().build().join()
        window.show().join()
    }
}
