package de.dasbabypixel.gamelauncher.lwjgl.window.glfw

import de.dasbabypixel.gamelauncher.api.launcherHandlesException
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.Vec2i
import de.dasbabypixel.gamelauncher.api.util.concurrent.*
import de.dasbabypixel.gamelauncher.api.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.lwjgl.window.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.withLock
import kotlin.concurrent.write

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

    private val ownedLock = ReentrantLock()

    /**
     * Lock that holds whether the window exists/is valid
     */
    private val lock = ReentrantReadWriteLock()
    override val implName = "GLFW"
    override val id = idCounter.incrementAndGet()
    private var owned = false
    @Volatile
    override var isVisible: Boolean = false
        private set
    override val frameSync = FrameSync()
    override lateinit var framebufferSize: Vec2i

    override fun startRendering(): LWJGLWindowImpl.RenderingInstance {
        creationFuture.join()
        makeCurrent()
        return this
    }

    override fun swapBuffers(): Boolean {
        lock.read {
            if (glfwId == 0L) return false
            glfwSwapBuffers(glfwId)
            return true
        }
    }

    override fun stopRendering() {
        destroyCurrent()
    }

    private val group: ThreadGroup = ThreadGroup.create("GLFWWindow-${id}", parent?.group ?: currentThread.group)
    override val renderThread: SimpleRenderThread = SimpleRenderThread(group, this)
    override val creationFuture = CompletableFuture<Unit>()
    private var renderImplementation: WindowRenderImplementation? = null
    private var renderImplementationRenderer: RenderImplementationRenderer? = null
    private var requestCloseCallback: ((window: LWJGLWindow) -> Unit)? = null
    private var framebufferSizeCallback: ((window: LWJGLWindow, width: Int, height: Int) -> Unit)? = null
    var glfwId: Long = 0L
        private set

    override fun changeRenderImplementation(renderImplementation: WindowRenderImplementation): CompletableFuture<Unit> {
        return runWindowRO {
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

    override fun requestFocus(): CompletableFuture<Unit> {
        return runWindowRO {
            glfwRequestWindowAttention(it)
        }
    }

    override fun forceFocus(): CompletableFuture<Unit> {
        return runWindowRO {
            glfwFocusWindow(it)
        }
    }

    internal fun create(): CompletableFuture<Unit> {
        return GLFWThread.submitGR { GLFWWindowCreator().run() }
    }

    override fun title(name: String): CompletableFuture<Unit> {
        return runWindowRO { glfwSetWindowTitle(it, name) }
    }

    override fun position(x: Int, y: Int): CompletableFuture<Unit> {
        return runWindowRO { glfwSetWindowPos(it, x, y) }
    }

    override fun requestCloseCallback(callback: (window: LWJGLWindow) -> Unit) {
        requestCloseCallback = callback
    }

    override fun framebufferSizeCallback(callback: (window: LWJGLWindow, width: Int, height: Int) -> Unit) {
        framebufferSizeCallback = callback
    }

    override fun cleanup0(): CompletableFuture<Unit> {
        return renderThread.cleanupAsync().thenCompose {
            runWindowW {
                glfwDestroyWindow(it)
                glfwId = 0L
            }.thenApplyAsync {
                Thread.sleep(500)
            }
        }
    }

    override fun show(): CompletableFuture<Unit> {
        return runWindowRO {
            glfwShowWindow(it)
            isVisible = true
            glfwSwapBuffers(it)
        }.thenApply {
            val frame = renderThread.startNextFrame()
            renderThread.awaitFrame(frame)
        }
    }

    override fun hide(): CompletableFuture<Unit> {
        return runWindowRO {
            isVisible = false
            glfwHideWindow(it)
        }
    }

    fun setTransparent(transparent: Boolean): CompletableFuture<Unit> {
        return runWindowRO {
            glfwSetWindowAttrib(it, GLFW_TRANSPARENT_FRAMEBUFFER, if (transparent) GLFW_TRUE else GLFW_FALSE)
        }
    }

    private inline fun <T> runWindowW(crossinline function: (Long) -> T): CompletableFuture<T> {
        return GLFWThread.submitGC {
            lock.write {
                val id = glfwId
                if (id == 0L) throw GameException("GLFW ID not initialized")
                function(id)
            }
        }
    }

    private inline fun <T> runWindowRO(crossinline function: (Long) -> T): CompletableFuture<T> {
        return GLFWThread.submitGC {
            lock.read {
                val id = glfwId
                if (id == 0L) throw GameException("GLFW ID not initialized")
                function(id)
            }
        }
    }

    fun makeCurrent() {
        val thread = currentThread
        if (thread !is Executor) throw IllegalThreadStateException("Thread must be an executor thread to make a window current: $thread")
        this.ownedLock.withLock {
            if (this.owned) throw IllegalStateException("Already owned")
            this.owned = true
        }
        glfwMakeContextCurrent(glfwId)
        currentWindow.set(this)
    }

    fun destroyCurrent() {
        val thread = currentThread
        if (thread !is Executor) throw IllegalThreadStateException("Owner thread is not an executor thread. This should not be possible")
        if (currentWindow.get() != this) throw IllegalThreadStateException("Tried to release context from other thread than owner thread")
        if (!this.owned) throw IllegalStateException("Not currently owned. Should be impossible")
        this.ownedLock.withLock { this.owned = false }
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

                val glfwId = glfwCreateWindow(startWidth, startHeight, "GameLauncher", 0, 0)
                if (glfwId == 0L) glfwError()
                lock.write {
                    this@GLFWWindow.glfwId = glfwId
                }

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