package de.dasbabypixel.gamelauncher.impl.window.glfw

import de.dasbabypixel.gamelauncher.impl.window.WindowBuilder
import de.dasbabypixel.gamelauncher.impl.window.WindowSystem
import de.dasbabypixel.gamelauncher.logging.LoggingInstance
import de.dasbabypixel.gamelauncher.logging.getLogger
import de.dasbabypixel.gamelauncher.service.ServiceRegistry
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.util.concurrent.allComplete
import de.dasbabypixel.gamelauncher.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker

class GLFWWindowSystem(
    tracker: ResourceTracker,
    val loggingInstance: LoggingInstance,
    val serviceRegistry: ServiceRegistry
) :
    AbstractGameResource(tracker), WindowSystem {
    private val logger by getLogger(loggingInstance)
    private var id: Int = 0
    val windows: MutableSet<GLFWWindow> = HashSet()
    override fun createWindow(): WindowBuilder {
        return GLFWWindowBuilder(tracker, loggingInstance, serviceRegistry, this)
    }

    override fun init() {
//        GLFW.glfwSetErrorCallback { errorCode, description ->
//            logger.error("GLFW Error {}: {}",
//                errorCode.toHexString(),
//                GLFWErrorCallback.getDescription(description))
//        }
    }

    override fun getVulkanExtensions(): Collection<String> {
        return GLFWThread.task.vulkanExtensions.join()
    }

    override fun takeOverInitialThread() {
        GLFWThread.takeOverByGLFW(loggingInstance, tracker, serviceRegistry)
    }

    override fun cleanup0(): CompletableFuture<Unit> {
        println("Cleaning up ${windows.size} windows")
        return windows.map { window ->
            window.cleanupAsync()
        }.allComplete().thenCompose {
            GLFWThread.task.cleanupAsync()
        }
    }

    fun nextId(): Int {
        GLFWThread.ensureOnThread()
        return id++
    }
}
