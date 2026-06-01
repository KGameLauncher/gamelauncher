package de.dasbabypixel.gamelauncher.impl

import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.concurrent.configureThirdPartyThread
import de.dasbabypixel.gamelauncher.impl.vulkan.VKInitializer
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.system.MemoryStack
import java.lang.Thread as JThread

fun main() {
    JThread.currentThread().configureThirdPartyThread()
    DesktopInitializer.init()

    if (!GLFW.glfwInit()) throw GameException("GLFW couldn't initialize")
    if (!GLFWVulkan.glfwVulkanSupported()) throw GameException("GLFWVulkan not supported")

    val vulkanExtensions = mutableSetOf<String>()

    vulkanExtensions.addAll(MemoryStack.stackPush().use {
        (GLFWVulkan.glfwGetRequiredInstanceExtensions()
            ?: throw GameException("GLFWVulkan not supported: Extensions NULL")).let { p ->
            val count = p.capacity()
            mutableSetOf<String>().also {
                for (i in 0 until count) {
                    it.add(p.getStringUTF8(i))
                }
            }
        }
    })

    VKInitializer.init(vulkanExtensions)

}
