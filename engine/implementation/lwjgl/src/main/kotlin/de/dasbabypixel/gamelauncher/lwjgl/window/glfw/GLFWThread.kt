package de.dasbabypixel.gamelauncher.lwjgl.window.glfw

import de.dasbabypixel.gamelauncher.api.util.concurrent.ExecutorThread
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.lwjgl.util.concurrent.InitialThread
import de.dasbabypixel.gamelauncher.lwjgl.window.WrappingExecutorThread
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.system.APIUtil

object GLFWThread : WrappingExecutorThread {
    override lateinit var handle: ExecutorThread
}

object GLFWThreadImplementation : InitialThread.Implementation {
    private val logger = getLogger<GLFWThread>("LWJGL")

    @OptIn(ExperimentalStdlibApi::class)
    private val errorCallback = object : GLFWErrorCallback() {
        private val ERROR_CODES = APIUtil.apiClassTokens(
            { _, value -> value in 0x10001..0x1ffff }, null, org.lwjgl.glfw.GLFW::class.java
        )

        override fun invoke(errorCode: Int, descriptionId: Long) {
            val description = getDescription(descriptionId)
            val error = ERROR_CODES[errorCode]!!
            logger.error("GLFW Error: {}({}) - {}", error, errorCode.toHexString(), description, Exception())
        }
    }

    @Volatile
    private var initialized = false

    override val name: String
        get() = "GLFW-Thread"

    override fun initialized(thread: ExecutorThread): ExecutorThread {
        GLFWThread.handle = thread
        return GLFWThread
    }

    override fun startExecuting() {
        logger.debug("Initializing GLFW")
        glfwSetErrorCallback(errorCallback)
        if (!glfwInit()) {
            throw IllegalStateException("Failed to initialize GLFW")
        }
        initialized = true
        @Suppress("UnusedExpression")
        GLFWMonitors // CL-Init GLFWMonitors
    }

    override fun workExecution() {
        // Events are already processed by glfwWaitEvents
    }

    override fun stopExecuting() {
        logger.debug("Terminating GLFW")
        errorCallback.free()
        initialized = false
        glfwTerminate()
    }

    override fun customSignal() {
        glfwPostEmptyEvent()
    }

    override fun customAwaitWork() {
        glfwWaitEvents()
    }
}
