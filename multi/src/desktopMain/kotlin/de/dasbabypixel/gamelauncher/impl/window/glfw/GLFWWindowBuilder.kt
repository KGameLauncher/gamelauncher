package de.dasbabypixel.gamelauncher.impl.window.glfw

import de.dasbabypixel.gamelauncher.api.math.Vec2i
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanAccess
import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import de.dasbabypixel.gamelauncher.impl.window.Window
import de.dasbabypixel.gamelauncher.impl.window.WindowBuilder
import de.dasbabypixel.gamelauncher.resource.ResourceTracker
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.system.MemoryStack

class GLFWWindowBuilder(val system: GLFWWindowSystem) : WindowBuilder {
    private var initialX: Int = Int.MIN_VALUE
    private var initialY: Int = Int.MIN_VALUE
    private var initialWidth: Int = 400
    private var initialHeight: Int = 400
    private var title: String = "Unnamed Window"
    private var resizable: Boolean = true

    override fun initialPosition(x: Int, y: Int) {
        initialX = x
        initialY = y
    }

    override fun initialSize(width: Int, height: Int) {
        initialWidth = width;
        initialHeight = height
    }

    override fun initialResizable(resizable: Boolean) {
        this.resizable = resizable
    }

    override fun build(): CompletableFuture<Window> {
        return GLFWThread.task.submitGC { createWindow() }
    }

    private fun createWindow(): GLFWWindow {
        GLFWThread.ensureOnThread()

        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API)
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, if (resizable) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)
        val handle = GLFW.glfwCreateWindow(initialWidth, initialHeight, title, 0L, 0L)
        if (handle == 0L) throw GameException("Failed to create window")

        val tracker = ResourceTracker.global

        val surface = VulkanAccess.instance.createSurface { stack, instance ->
            val pSurface = stack.mallocLong(1)
            GLFWVulkan.glfwCreateWindowSurface(instance.instance, handle, instance.pAllocator, pSurface).vkValidate()
            pSurface.get(0)
        }
        val id = system.nextId()

        val iconified = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE
        val maximized = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE
        val framebufferSize = MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)
            GLFW.glfwGetFramebufferSize(handle, pWidth, pHeight)
            Vec2i(pWidth.get(0), pHeight.get(0))
        }
        return GLFWWindow(system, id, tracker, handle, framebufferSize, iconified, maximized, surface).also {
            system.windows.add(it)
        }
    }
}