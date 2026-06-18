package de.dasbabypixel.gamelauncher.impl.window.glfw

import de.dasbabypixel.gamelauncher.api.lifecycle.ShutdownHandler
import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.api.util.function.GameFunction
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKSurface
import de.dasbabypixel.gamelauncher.impl.window.Window
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWFramebufferSizeCallback
import org.lwjgl.glfw.GLFWWindowCloseCallback
import org.lwjgl.glfw.GLFWWindowIconifyCallback
import org.lwjgl.glfw.GLFWWindowMaximizeCallback
import org.lwjgl.system.Callback
import org.lwjgl.system.MemoryStack
import java.util.concurrent.ForkJoinPool
import kotlin.concurrent.Volatile

class GLFWWindow(
    val system: GLFWWindowSystem,
    val id: Int,
    tracker: ResourceTracker,
    private val handle: Long,
    override val surface: VKSurface
) : AbstractGameResource(tracker), Window {
    private var valid: Boolean = true
    private val callbacks: List<CB<*>> = callbackTypes.map { CB(it) }
    private var iconified: Boolean = false
    private var maximized: Boolean = false
    private var framebufferWidth: Int = -1
    private var framebufferHeight: Int = -1

    private class CB<T>(val type: CBType<T>, var value: T? = null) {
        fun create(window: GLFWWindow) {
            value = type.create(window)
        }

        fun free() {
            type.free(value!!)
        }

        fun register(handle: Long) {
            type.register(handle, value!!)
        }
    }

    private class CBType<T>(
        val create: (GLFWWindow) -> T, val register: (Long, T) -> Unit, val free: (T) -> Unit
    )

    internal fun glfwCreate() {
        GLFWThread.ensureOnThread()
        callbacks.forEach {
            it.create(this)
        }
        callbacks.forEach {
            it.register(handle)
        }
        iconified = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE
        maximized = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE
        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)
            GLFW.glfwGetFramebufferSize(handle, pWidth, pHeight)
            framebufferWidth = pWidth.get(0)
            framebufferHeight = pHeight.get(0)
        }
    }

    @Volatile
    private var visible: Boolean = false
    override fun cleanup0(): CompletableFuture<Unit> {
        return surface.cleanupAsync().thenComposeAsync(ForkJoinPool.commonPool()) {
            submit {
                GLFW.glfwDestroyWindow(handle)
                callbacks.forEach { it.free() }
                system.windows.remove(this)
                valid = false
            }
        }
    }

    override fun show(): CompletableFuture<Unit> {
        return submit { GLFW.glfwShowWindow(it) }
    }

    override fun hide(): CompletableFuture<Unit> {
        return submit { GLFW.glfwHideWindow(it) }
    }

    override val isVisible: Boolean
        get() = visible

    override fun fetchVisible(): CompletableFuture<Boolean> {
        return submit { visible }
    }

    override fun fetchMaximized(): CompletableFuture<Boolean> {
        return submit { maximized }
    }

    override fun fetchIconified(): CompletableFuture<Boolean> {
        return submit { iconified }
    }

    private fun <T> submit(c: GameFunction<Long, T>): CompletableFuture<T> {
        return GLFWThread.task.submitGC {
            if (!valid) throw GameException("Window cleaned up")
            c(handle)
        }
    }

    private val identifier: String = "window-$id"

    companion object {
        private val logger by getLogger()
        private val callbackTypes: MutableList<CBType<*>> = ArrayList()

        init {
            windowCB(GLFW::glfwSetWindowMaximizeCallback) { window ->
                GLFWWindowMaximizeCallback.create { _, maximized ->
                    logger.debug("Maximized status for {} changed from {} to {}",
                        window.identifier,
                        window.maximized,
                        maximized)
                    window.maximized = maximized
                }
            }
            windowCB(GLFW::glfwSetWindowCloseCallback) { window ->
                GLFWWindowCloseCallback.create {
                    logger.debug("User requested close for {}", window.identifier)

                    ShutdownHandler.shutdownGracefully()
                }
            }
            windowCB(GLFW::glfwSetWindowIconifyCallback) { window ->
                GLFWWindowIconifyCallback.create { _, iconified ->
                    logger.debug("Iconified status for {} changed from {} to {}",
                        window.identifier,
                        window.iconified,
                        iconified)
                    window.iconified = iconified
                }
            }
            windowCB(GLFW::glfwSetFramebufferSizeCallback) { window ->
                GLFWFramebufferSizeCallback.create { _, width, height ->
                    logger.debug("Framebuffer size for {} changed from {}x{} to {}x{}",
                        window.identifier,
                        window.framebufferWidth,
                        window.framebufferHeight,
                        width,
                        height)
                    window.framebufferWidth = width
                    window.framebufferHeight = height
                }
            }
        }

        private fun <T : Callback> windowCB(
            register: (Long, T) -> Unit, create: (GLFWWindow) -> T
        ) {
            callbackTypes.add(CBType(create, register, Callback::free))
        }
    }
}
