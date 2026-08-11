package de.dasbabypixel.gamelauncher.impl.window.glfw

import de.dasbabypixel.gamelauncher.logging.getLogger
import de.dasbabypixel.gamelauncher.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.resource.SimpleResourceTracker
import de.dasbabypixel.gamelauncher.service.ServiceRegistry
import de.dasbabypixel.gamelauncher.util.GameException
import de.dasbabypixel.gamelauncher.util.concurrent.AbstractExecutorThread
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.util.concurrent.configureThirdPartyThread
import de.dasbabypixel.gamelauncher.util.concurrent.currentThread
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.glfwInit
import org.lwjgl.glfw.GLFW.glfwSetErrorCallback
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.system.APIUtil
import org.lwjgl.system.MemoryStack

class GLFWThread(tracker: ResourceTracker, serviceRegistry: ServiceRegistry, thread: Thread) :
    AbstractExecutorThread(tracker, serviceRegistry, thread, true) {

    private val logger by getLogger("LWJGL")
    val vulkanExtensions = CompletableFuture<Set<String>>()

    private val errorCallback = object : GLFWErrorCallback() {
        private val ERROR_CODES =
            APIUtil.apiClassTokens({ _, value -> value in 0x10001..0x1ffff }, null, GLFW::class.java)

        override fun invoke(errorCode: Int, descriptionId: Long) {
            val description = getDescription(descriptionId)
            val error = ERROR_CODES[errorCode]!!
            logger.error("GLFW Error: {}({}) - {}", error, errorCode.toHexString(), description, Exception())
        }
    }

    @Volatile
    private var initialized = false

    override fun startExecuting() {
        track()
        logger.debug("Initializing GLFW")

        if (!glfwInit()) throw GameException("GLFW couldn't initialize")
        if (!GLFWVulkan.glfwVulkanSupported()) throw GameException("GLFWVulkan not supported")
        glfwSetErrorCallback(errorCallback)
        initialized = true
//        GLFWMonitors.init()

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
        this.vulkanExtensions.complete(vulkanExtensions)
    }

    override fun workExecution() {
        // Events are already processed by glfwWaitEvents
    }

    override fun stopExecuting() {
        logger.debug("Terminating GLFW")
        errorCallback.free()
        initialized = false
        GLFW.glfwTerminate()
    }

    override fun customSignal() {
        GLFW.glfwPostEmptyEvent()
    }

    override fun customAwaitWork() {
        GLFW.glfwWaitEvents()
    }

    companion object {
        private var thread = CompletableFuture<Thread>()

        /**
         * Let GLFW take over this thread. This will not return until GLFW is terminated.
         */
        fun takeOverByGLFW() {
            val thread = java.lang.Thread.currentThread().configureThirdPartyThread({ thread ->
                GLFWThread(SimpleResourceTracker.global, ServiceRegistry.global, thread)
            }, overwrite = true)
            thread.name = "GLFW-Thread"
            this.thread.complete(thread)
            thread.task.run()
        }

        fun ensureOnThread() {
            if (currentThread.task !is GLFWThread) throw IllegalStateException("Not on GLFW Thread")
        }

        val task: GLFWThread
            get() = thread.join().task as GLFWThread
    }
}