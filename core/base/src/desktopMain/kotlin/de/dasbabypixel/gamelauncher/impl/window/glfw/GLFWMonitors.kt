package de.dasbabypixel.gamelauncher.impl.window.glfw

import de.dasbabypixel.gamelauncher.logging.LoggingInstance
import de.dasbabypixel.gamelauncher.logging.getLogger
import org.lwjgl.glfw.GLFW.GLFW_CONNECTED
import org.lwjgl.glfw.GLFW.GLFW_DISCONNECTED
import org.lwjgl.glfw.GLFW.glfwGetMonitorContentScale
import org.lwjgl.glfw.GLFW.glfwGetMonitorName
import org.lwjgl.glfw.GLFW.glfwGetMonitorPos
import org.lwjgl.glfw.GLFW.glfwGetMonitors
import org.lwjgl.glfw.GLFW.glfwGetVideoMode
import org.lwjgl.glfw.GLFW.glfwSetMonitorCallback
import org.lwjgl.glfw.GLFWMonitorCallback
import java.util.concurrent.CopyOnWriteArrayList

class GLFWMonitors(loggingInstance: LoggingInstance) {
    private val logger by getLogger<GLFWMonitors>(loggingInstance, "LWJGL")
    private val callback = object : GLFWMonitorCallback() {
        override fun invoke(monitor: Long, event: Int) {
            when (event) {
                GLFW_CONNECTED -> connectMonitor(monitor)
                GLFW_DISCONNECTED -> disconnectMonitor(monitor)
                else -> logger.error("Unknown event: $event")
            }
        }
    }
    private val monitors = CopyOnWriteArrayList<GLFWMonitor>()

    fun init() {
        GLFWThread.ensureOnThread()
        glfwSetMonitorCallback(callback)
        val monitors = glfwGetMonitors() ?: throw IllegalStateException("Unable to fetch monitors")
        while (monitors.hasRemaining()) {
            connectMonitor(monitors.get())
        }
    }

    private fun connectMonitor(monitorId: Long) {
        GLFWThread.ensureOnThread()
        val x = intArrayOf(0)
        val y = intArrayOf(0)
        glfwGetMonitorPos(monitorId, x, y)
        val name =
            glfwGetMonitorName(monitorId) ?: throw IllegalStateException("Monitor without name")
        val sx = floatArrayOf(0F)
        val sy = floatArrayOf(0F)
        glfwGetMonitorContentScale(monitorId, sx, sy)
        val vidMode =
            glfwGetVideoMode(monitorId) ?: throw IllegalStateException("Monitor without VideoMode")
        val monitor = GLFWMonitor(
            name,
            x[0],
            y[0],
            vidMode.width(),
            vidMode.height(),
            sx[0],
            sy[0],
            monitorId,
            VideoMode(vidMode.width(), vidMode.height(), vidMode.refreshRate())
        )
        monitors.add(monitor)
        logger.info("New monitor connected: {}", monitor)
    }

    private fun disconnectMonitor(monitor: Long) {
        GLFWThread.ensureOnThread()
        if (monitors.removeIf {
                it.glfwId == monitor
            }) {
            logger.info("Monitor disconnected: {}", monitor)
        }
    }
}

data class GLFWMonitor(
    val name: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val scaleX: Float,
    val scaleY: Float,
    val glfwId: Long,
    val videoMode: VideoMode
)

data class VideoMode(
    val width: Int, val height: Int, val refreshRate: Int
)