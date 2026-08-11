package de.dasbabypixel.gamelauncher.impl.window.glfw

import de.dasbabypixel.gamelauncher.impl.window.WindowBuilder
import de.dasbabypixel.gamelauncher.impl.window.WindowSystem
import de.dasbabypixel.gamelauncher.logging.getLogger
import de.dasbabypixel.gamelauncher.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.resource.SimpleResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.util.concurrent.allComplete

class GLFWWindowSystem : AbstractGameResource(SimpleResourceTracker.global), WindowSystem {
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
