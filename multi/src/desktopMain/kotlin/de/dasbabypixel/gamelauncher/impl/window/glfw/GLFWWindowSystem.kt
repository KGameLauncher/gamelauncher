package de.dasbabypixel.gamelauncher.impl.window.glfw

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.api.util.concurrent.allComplete
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.impl.window.WindowBuilder
import de.dasbabypixel.gamelauncher.impl.window.WindowSystem

class GLFWWindowSystem : AbstractGameResource(ResourceTracker.global), WindowSystem {
    private val logger by getLogger()
    private var id: Int = 0
    val windows: MutableSet<GLFWWindow> = HashSet()
    override fun createWindow(): WindowBuilder {
        return GLFWWindowBuilder(this)
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
        GLFWThread.takeOverByGLFW()
    }

    override fun cleanup0(): CompletableFuture<Unit> {
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
