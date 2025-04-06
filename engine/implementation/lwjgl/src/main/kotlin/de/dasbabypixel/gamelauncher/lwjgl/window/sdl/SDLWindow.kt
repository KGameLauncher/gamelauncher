package de.dasbabypixel.gamelauncher.lwjgl.window.sdl

import de.dasbabypixel.gamelauncher.api.config.Config
import de.dasbabypixel.gamelauncher.api.launcherHandlesException
import de.dasbabypixel.gamelauncher.api.util.concurrent.FrameSync
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.api.util.concurrent.currentThread
import de.dasbabypixel.gamelauncher.api.util.function.GameFunction
import de.dasbabypixel.gamelauncher.api.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.lwjgl.window.*
import org.lwjgl.opengl.GL
import org.lwjgl.sdl.SDLVideo.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class SDLWindow : AbstractGameResource(), LWJGLWindowImpl {
    companion object {
        private val idCounter = AtomicInteger()
    }

    override val frameSync: FrameSync = FrameSync()
    override val implName: String = "SDL"
    override val id = idCounter.incrementAndGet()
    private val group: ThreadGroup = ThreadGroup.create("SDLWindow-{$id}")
    private var requestCloseCallback: ((window: LWJGLWindow) -> Unit)? = null
    private var framebufferSizeCallback: ((window: LWJGLWindow, width: Int, height: Int) -> Unit)? = null
    private var renderImplementation: WindowRenderImplementation? = null
    private var renderImplementationRenderer: RenderImplementationRenderer? = null
    override val renderThread = SimpleRenderThread(group, this)

    var sdlWindowPtr: Long = 0L
        private set
    var sdlWindowId: Int = 0
        private set

    fun handleCloseRequested() {
        val cb = requestCloseCallback ?: return
        launcherHandlesException { cb(this) }
    }

    fun handleFramebufferUpdate(w: Int, h: Int) {
        renderThread.framebufferUpdate(w, h)
        framebufferSizeCallback?.invoke(this, w, h)
    }

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
        return SDLThread.submitGR {
            val window = SDL_CreateWindow(
                Config.NAME.value,
                600,
                600,
                SDL_WINDOW_RESIZABLE or SDL_WINDOW_HIDDEN or SDL_WINDOW_TRANSPARENT or SDL_WINDOW_OPENGL
            )
            sdlWindowPtr = window
            sdlWindowId = SDL_GetWindowID(sdlWindowPtr)
            SDLThreadImplementation.addWindow(this)
        }
    }

    override fun title(name: String): CompletableFuture<Unit> {
        return runWindow { SDL_SetWindowTitle(it, name) }
    }

    override fun position(x: Int, y: Int): CompletableFuture<Unit> {
        return runWindow { SDL_SetWindowPosition(it, x, y) }
    }

    override fun startRendering(): LWJGLWindowImpl.RenderingInstance {
        return object : LWJGLWindowImpl.RenderingInstance {
            val ctx: Long = runWindow {
                val ctx = SDL_GL_CreateContext(sdlWindowPtr)
                GL.createCapabilities()
                GL.setCapabilities(null)
                SDL_GL_MakeCurrent(sdlWindowPtr, 0L)
                ctx
            }.join()

            init {
                SDL_GL_MakeCurrent(sdlWindowPtr, ctx)
                println("current int ${currentThread.name}")
            }

            override fun swapBuffers() {
                SDL_GL_SwapWindow(sdlWindowPtr)
            }

            override fun stopRendering() {
                SDL_GL_MakeCurrent(sdlWindowPtr, 0L)
                runWindow { SDL_GL_DestroyContext(ctx) }.join()
            }
        }
    }

    override fun framebufferSizeCallback(callback: (window: LWJGLWindow, width: Int, height: Int) -> Unit) {
        framebufferSizeCallback = callback
    }

    override fun requestCloseCallback(callback: (window: LWJGLWindow) -> Unit) {
        requestCloseCallback = callback
    }

    override fun show(): CompletableFuture<Unit> {
        return runWindow { SDL_ShowWindow(it) }
    }

    override fun hide(): CompletableFuture<Unit> {
        return runWindow { SDL_HideWindow(it) }
    }

    private fun exit(): CompletableFuture<Unit> {
        return runWindow {
            SDLThreadImplementation.removeWindow(this)
            val ptr = sdlWindowPtr
            sdlWindowPtr = 0L
            sdlWindowId = 0
            SDL_DestroyWindow(ptr)
        }
    }

    private fun <T> runWindow(consumer: GameFunction<Long, T>): CompletableFuture<T> {
        return SDLThread.submitGC { consumer.apply(sdlWindowPtr) }
    }

    override fun cleanup0(): CompletableFuture<Unit> {
        return exit()
    }
}