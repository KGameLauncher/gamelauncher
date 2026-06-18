package de.dasbabypixel.gamelauncher.impl.window.glfw

import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.VKAccess
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKSurface
import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import de.dasbabypixel.gamelauncher.impl.window.Window
import de.dasbabypixel.gamelauncher.impl.window.WindowBuilder
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

        val surface = MemoryStack.stackPush().use { stack ->
            val vulkanInstance = VKAccess.instance
            val vkInstance = vulkanInstance.instance
            val pSurface = stack.mallocLong(1)
            GLFWVulkan.glfwCreateWindowSurface(vkInstance.instance,
                handle,
                vkInstance.pAllocator,
                pSurface).vkValidate()
            VKSurface(tracker, pSurface.get(0), vulkanInstance)
        }
        val id = system.nextId()
        return GLFWWindow(system, id, tracker, handle, surface).also {
            system.windows.add(it)
            it.glfwCreate()
        }
    }
}