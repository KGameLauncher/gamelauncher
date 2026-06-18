package de.dasbabypixel.gamelauncher.impl.window.glfw

import de.dasbabypixel.gamelauncher.api.lifecycle.ShutdownHandler
import de.dasbabypixel.gamelauncher.api.math.Vec2i
import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.api.util.function.GameFunction
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanSurface
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanSwapChain
import de.dasbabypixel.gamelauncher.impl.window.Window
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWFramebufferSizeCallback
import org.lwjgl.glfw.GLFWWindowCloseCallback
import org.lwjgl.glfw.GLFWWindowIconifyCallback
import org.lwjgl.glfw.GLFWWindowMaximizeCallback
import org.lwjgl.system.Callback
import java.util.concurrent.ForkJoinPool
import kotlin.concurrent.Volatile

class GLFWWindow(
    val system: GLFWWindowSystem,
    id: Int,
    tracker: ResourceTracker,
    private val handle: Long,
    framebufferSize: Vec2i,
    iconified: Boolean,
    maximized: Boolean,
    swapChain: VulkanSwapChain,
    override val surface: VulkanSurface
) : AbstractGameResource(tracker), Window {
    private var valid: Boolean = true
    private val callbacks: List<CB<*>> = callbackTypes.map { CB(it) }
    private var iconified: Boolean = iconified
    private var maximized: Boolean = maximized
    private var swapChain: VulkanSwapChain = swapChain
    var framebufferSize: Vec2i = framebufferSize
        private set
        get() = field.also { GLFWThread.ensureOnThread() }

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

    init {
        GLFWThread.ensureOnThread()
        callbacks.forEach {
            it.create(this)
        }
        callbacks.forEach {
            it.register(handle)
        }
    }

    private class CBType<T>(
        val create: (GLFWWindow) -> T, val register: (Long, T) -> Unit, val free: (T) -> Unit
    )

    @Volatile
    private var visible: Boolean = false
    override fun cleanup0(): CompletableFuture<Unit> {
        return swapChain.cleanupAsync()
            .thenCompose { surface.cleanupAsync() }
            .thenComposeAsync(ForkJoinPool.commonPool()) {
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
                        window.framebufferSize.x,
                        window.framebufferSize.y,
                        width,
                        height)
                    window.framebufferSize = Vec2i(width, height)
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
