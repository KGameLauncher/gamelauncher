package de.dasbabypixel.gamelauncher.impl.window.glfw

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.api.lifecycle.ShutdownHandler
import de.dasbabypixel.gamelauncher.util.math.Vec2i
import de.dasbabypixel.gamelauncher.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanSurface
import de.dasbabypixel.gamelauncher.impl.window.Window
import de.dasbabypixel.gamelauncher.logging.LoggingInstance
import de.dasbabypixel.gamelauncher.logging.getLogger
import de.dasbabypixel.gamelauncher.service.ServiceRegistry
import de.dasbabypixel.gamelauncher.util.GameException
import de.dasbabypixel.gamelauncher.util.function.GameFunction
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
    loggingInstance: LoggingInstance,
    val serviceRegistry: ServiceRegistry,
    id: Int,
    tracker: ResourceTracker,
    private val handle: Long,
    framebufferSize: Vec2i,
    iconified: Boolean,
    maximized: Boolean,
    override val surface: VulkanSurface
) : AbstractGameResource(tracker), Window {
    private val logger by getLogger(loggingInstance)
    private var valid: Boolean = true
    private val callbacks: List<CB<*>> = callbackTypes.map { CB(it) }
    private var iconified: Boolean = iconified
    private var maximized: Boolean = maximized
    val identifier: String = "window-$id"

    @Volatile
    var framebufferSize: Vec2i = framebufferSize
        private set
    val renderThread: GLFWRenderThread =
        GLFWRenderThread.create(loggingInstance, tracker, serviceRegistry, this)

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
        renderThread.start()
    }

    private class CBType<T>(
        val create: (GLFWWindow) -> T, val register: (Long, T) -> Unit, val free: (T) -> Unit
    )

    @Volatile
    private var visible: Boolean = false
    override fun cleanup0(): CompletableFuture<Unit> {
        println("Begin cleanup window")
        return renderThread.cleanupAsync()
            .thenCompose { surface.cleanupAsync() }
            .thenComposeAsync(ForkJoinPool.commonPool()) {
                submit {
                    GLFW.glfwDestroyWindow(handle)
                    println("Post glfw destroy window")
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

    companion object {
        private val callbackTypes: MutableList<CBType<*>> = ArrayList()

        init {
            windowCB(GLFW::glfwSetWindowMaximizeCallback) { window ->
                GLFWWindowMaximizeCallback.create { _, maximized ->
                    window.logger.debug(
                        "Maximized status for {} changed from {} to {}",
                        window.identifier,
                        window.maximized,
                        maximized
                    )
                    window.maximized = maximized
                }
            }
            windowCB(GLFW::glfwSetWindowCloseCallback) { window ->
                GLFWWindowCloseCallback.create {
                    window.logger.debug("User requested close for {}", window.identifier)

                    window.serviceRegistry.singleInstance<GameLauncher>().shutdownGracefully()
                }
            }
            windowCB(GLFW::glfwSetWindowIconifyCallback) { window ->
                GLFWWindowIconifyCallback.create { _, iconified ->
                    window.logger.debug(
                        "Iconified status for {} changed from {} to {}",
                        window.identifier,
                        window.iconified,
                        iconified
                    )
                    window.iconified = iconified
                }
            }
            windowCB(GLFW::glfwSetFramebufferSizeCallback) { window ->
                GLFWFramebufferSizeCallback.create { _, width, height ->
                    window.logger.debug(
                        "Framebuffer size for {} changed from {}x{} to {}x{}",
                        window.identifier,
                        window.framebufferSize.x,
                        window.framebufferSize.y,
                        width,
                        height
                    )
                    window.framebufferSize = Vec2i(width, height)
                    window.renderThread.framebufferResized()
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
