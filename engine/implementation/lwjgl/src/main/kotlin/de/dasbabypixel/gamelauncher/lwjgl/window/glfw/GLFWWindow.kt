package de.dasbabypixel.gamelauncher.lwjgl.window.glfw

import de.dasbabypixel.gamelauncher.api.launcherHandlesException
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.Vec2i
import de.dasbabypixel.gamelauncher.api.util.concurrent.Executor
import de.dasbabypixel.gamelauncher.api.util.concurrent.FrameSync
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.api.util.concurrent.currentThread
import de.dasbabypixel.gamelauncher.api.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.lwjgl.window.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class GLFWWindow internal constructor(parent: GLFWWindow?) : AbstractGameResource(), LWJGLWindowImpl,
    LWJGLWindowImpl.RenderingInstance {
    companion object {
        private val idCounter = AtomicInteger()
        private val currentWindow = ThreadLocal<GLFWWindow>()
    }

    private val sharedWindows: SharedWindows

    init {
        this.sharedWindows = parent?.sharedWindows ?: SharedWindows()
        this.sharedWindows.add(this)
    }

    private val modifyLock = ReentrantLock()
    override val implName = "GLFW"
    override val id = idCounter.incrementAndGet()
    private var owned = false
    override val frameSync = FrameSync()
    override lateinit var framebufferSize: Vec2i

    override fun startRendering(): LWJGLWindowImpl.RenderingInstance {
        creationFuture.join()
        makeCurrent()
        return this
    }

    override fun swapBuffers() {
        println("swap $glfwId")
        glfwSwapBuffers(glfwId)
    }

    override fun stopRendering() {
        destroyCurrent()
    }

    private val group: ThreadGroup = ThreadGroup.create("GLFWWindow-${id}", parent?.group ?: currentThread.group)
    override val renderThread: SimpleRenderThread = SimpleRenderThread(group, this)
    val creationFuture = CompletableFuture<Unit>()
    private var renderImplementation: WindowRenderImplementation? = null
    private var renderImplementationRenderer: RenderImplementationRenderer? = null
    private var requestCloseCallback: ((window: LWJGLWindow) -> Unit)? = null
    private var framebufferSizeCallback: ((window: LWJGLWindow, width: Int, height: Int) -> Unit)? = null
    var glfwId: Long = 0L
        private set

    override fun changeRenderImplementation(renderImplementation: WindowRenderImplementation): CompletableFuture<Unit> {
        return runWindow {
            this.renderImplementation?.let {
                renderThread.setRenderer(null)
                it.disable(this, renderImplementationRenderer!!)
            }
            this.renderImplementation = renderImplementation
            val renderer = renderImplementation.enable(this)
            renderImplementationRenderer = renderer
            renderThread.setRenderer(renderer)
        }
    }

    internal fun create(): CompletableFuture<Unit> {
        return GLFWThread.submitGR { GLFWWindowCreator().run() }
    }

    override fun title(name: String): CompletableFuture<Unit> {
        return runWindow { glfwSetWindowTitle(it, name) }
    }

    override fun position(x: Int, y: Int): CompletableFuture<Unit> {
        return runWindow { glfwSetWindowPos(it, x, y) }
    }

    override fun requestCloseCallback(callback: (window: LWJGLWindow) -> Unit) {
        requestCloseCallback = callback
    }

    override fun framebufferSizeCallback(callback: (window: LWJGLWindow, width: Int, height: Int) -> Unit) {
        framebufferSizeCallback = callback
    }

    override fun cleanup0(): CompletableFuture<Unit> {
        return renderThread.cleanup().thenCompose {
            runWindow {
                glfwDestroyWindow(it)
            }
        }
    }

    override fun show(): CompletableFuture<Unit> {
        return runWindow {
            glfwShowWindow(it)
            glfwSwapBuffers(it)
            val frame = renderThread.startNextFrame()
            renderThread.awaitFrame(frame)
        }
    }

    override fun hide(): CompletableFuture<Unit> {
        return runWindow { glfwHideWindow(it) }
    }

    fun setTransparent(transparent: Boolean): CompletableFuture<Unit> {
        return runWindow {
            glfwSetWindowAttrib(glfwId, GLFW_TRANSPARENT_FRAMEBUFFER, if (transparent) GLFW_TRUE else GLFW_FALSE)
        }
    }

    private inline fun <T> runWindow(crossinline function: (Long) -> T): CompletableFuture<T> {
        return GLFWThread.submitGC {
            val id = glfwId
            if (id == 0L) throw GameException("GLFW ID not initialized")
            function(id)
        }
    }

    fun makeCurrent() {
        val thread = currentThread
        if (thread !is Executor) throw IllegalThreadStateException("Thread must be an executor thread to make a window current: $thread")
        val lock = this.modifyLock
        lock.lock()
        try {
            if (this.owned) throw IllegalStateException("Already owned")
            this.owned = true
        } finally {
            lock.unlock()
        }
        glfwMakeContextCurrent(glfwId)
        currentWindow.set(this)
    }

    fun destroyCurrent() {
        val thread = currentThread
        if (thread !is Executor) throw IllegalThreadStateException("Owner thread is not an executor thread. This should not be possible")
        if (currentWindow.get() != this) throw IllegalThreadStateException("Tried to release context from other thread than owner thread")
        if (!this.owned) throw IllegalStateException("Not currently owned. Should be impossible")
        val lock = this.modifyLock
        lock.lock()
        try {
            this.owned = false
        } finally {
            lock.unlock()
        }
        glfwMakeContextCurrent(0L)
        currentWindow.remove()
    }

    private fun glfwError(): Nothing {
        val buf = MemoryUtil.memAllocPointer(1)
        glfwGetError(buf)
        val error = buf.stringUTF8
        MemoryUtil.memFree(buf)
        throw GameException("GLFW Error: $error")
    }

    private inner class GLFWWindowCreator : Runnable {

        override fun run() {
            try {
                glfwDefaultWindowHints()
                glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE)
                glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
                glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE)
                glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_FALSE)

                val primaryMonitorId = glfwGetPrimaryMonitor()
                val primaryMode = glfwGetVideoMode(primaryMonitorId)!!
                glfwWindowHint(GLFW_RED_BITS, primaryMode.redBits())
                glfwWindowHint(GLFW_BLUE_BITS, primaryMode.blueBits())
                glfwWindowHint(GLFW_GREEN_BITS, primaryMode.greenBits())
                glfwWindowHint(GLFW_REFRESH_RATE, primaryMode.refreshRate())

                val startWidth = primaryMode.width() / 2
                val startHeight = primaryMode.height() / 2

                glfwId = glfwCreateWindow(startWidth, startHeight, "GameLauncher", 0, 0)
                if (glfwId == 0L) glfwError()

                glfwSetWindowSizeLimits(glfwId, 1, 1, GLFW_DONT_CARE, GLFW_DONT_CARE)

                glfwSetWindowCloseCallback(glfwId) {
                    val cb = requestCloseCallback ?: return@glfwSetWindowCloseCallback
                    launcherHandlesException { cb(this@GLFWWindow) }
                }
                glfwSetFramebufferSizeCallback(glfwId) { id, w, h ->
                    val cb = framebufferSizeCallback
                    framebufferSize = Vec2i(w, h)
                    launcherHandlesException {
                        renderThread.framebufferUpdate(w, h)
                        cb?.invoke(this@GLFWWindow, w, h)
                    }
                }
                MemoryStack.stackPush().use {
                    val width = it.mallocInt(1)
                    val height = it.mallocInt(1)
                    glfwGetFramebufferSize(glfwId, width, height)
                    framebufferSize = Vec2i(width.get(0), height.get(0))
                }
            } catch (t: Throwable) {
                creationFuture.completeExceptionally(t)
                throw t
            }
            creationFuture.complete(Unit)
        }
    }

}

private class SharedWindows {
    private val sharedWindows: CopyOnWriteArrayList<GLFWWindow> = CopyOnWriteArrayList()
    val lock = ReentrantLock()

    fun add(window: GLFWWindow) {
        sharedWindows.add(window)
    }
}