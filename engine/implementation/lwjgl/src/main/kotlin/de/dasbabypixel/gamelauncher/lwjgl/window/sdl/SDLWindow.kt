package de.dasbabypixel.gamelauncher.lwjgl.window.sdl

import de.dasbabypixel.gamelauncher.api.config.Config
import de.dasbabypixel.gamelauncher.api.launcherHandlesException
import de.dasbabypixel.gamelauncher.api.util.Vec2i
import de.dasbabypixel.gamelauncher.api.util.concurrent.FrameSync
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.api.util.concurrent.currentThread
import de.dasbabypixel.gamelauncher.api.util.function.GameFunction
import de.dasbabypixel.gamelauncher.api.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.lwjgl.window.*
import org.lwjgl.opengl.GL
import org.lwjgl.sdl.SDLVideo
import org.lwjgl.sdl.SDLVideo.*
import org.lwjgl.system.MemoryStack
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

class SDLWindow : AbstractGameResource(), LWJGLWindowImpl {
    companion object {
        private val idCounter = AtomicInteger()
    }

    override val frameSync: FrameSync = FrameSync()
    override val implName: String = "SDL"
    override val id = idCounter.incrementAndGet()
    override lateinit var framebufferSize: Vec2i
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
        framebufferSize = Vec2i(w, h)
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
            if (window == 0L) checkError()
            sdlWindowPtr = window
            sdlWindowId = SDL_GetWindowID(sdlWindowPtr)
            framebufferSize = MemoryStack.stackPush().use {
                val width = it.mallocInt(1)
                val height = it.mallocInt(1)
                if (!SDL_GetWindowSizeInPixels(sdlWindowPtr, width, height)) checkError()
                Vec2i(width.get(0), height.get(0))
            }
            SDLThreadImplementation.addWindow(this)
        }
    }

    override fun title(name: String): CompletableFuture<Unit> {
        return runWindow {
            if (!SDL_SetWindowTitle(it, name)) checkError()
        }
    }

    override fun position(x: Int, y: Int): CompletableFuture<Unit> {
        return runWindow {
            if (!SDL_SetWindowPosition(it, x, y)) checkError()
        }
    }

    override fun startRendering(): LWJGLWindowImpl.RenderingInstance {
        return object : LWJGLWindowImpl.RenderingInstance {
            val ctx: Long = runWindow {
                val ctx = SDL_GL_CreateContext(sdlWindowPtr)
                if (ctx == 0L) checkError()
                GL.createCapabilities()
                GL.setCapabilities(null)
                if (!SDL_GL_MakeCurrent(sdlWindowPtr, 0L)) checkError()
                ctx
            }.join()

            init {
                if (!SDL_GL_MakeCurrent(sdlWindowPtr, ctx)) checkError()
                println("current int ${currentThread.name}")
            }

            override fun swapBuffers() {
                if (!SDL_GL_SwapWindow(sdlWindowPtr)) checkError()
            }

            override fun stopRendering() {
                if (!SDL_GL_MakeCurrent(sdlWindowPtr, 0L)) checkError()
                runWindow {
                    if (!SDL_GL_DestroyContext(ctx)) checkError()
                }.join()
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
        return runWindow {
            Thread.sleep(2000)
            if (!SDL_ShowWindow(it)) checkError()
            val frame = renderThread.startNextFrame()
            renderThread.awaitFrame(frame)
        }
    }

    override fun hide(): CompletableFuture<Unit> {
        return runWindow {
            if (!SDL_HideWindow(it)) checkError()
        }
    }

    private fun <T> runWindow(consumer: GameFunction<Long, T>): CompletableFuture<T> {
        return SDLThread.submitGC { consumer.apply(sdlWindowPtr) }
    }

    override fun cleanup0(): CompletableFuture<Unit> {
        return renderThread.cleanup().thenCompose {
            runWindow {
                SDLThreadImplementation.removeWindow(this)
                val ptr = sdlWindowPtr
                sdlWindowPtr = 0L
                sdlWindowId = 0
                SDL_DestroyWindow(ptr)
            }
        }
    }
}