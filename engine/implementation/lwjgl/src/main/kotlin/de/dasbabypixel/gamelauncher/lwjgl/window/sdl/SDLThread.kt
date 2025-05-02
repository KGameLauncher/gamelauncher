package de.dasbabypixel.gamelauncher.lwjgl.window.sdl

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.concurrent.ExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.currentThread
import de.dasbabypixel.gamelauncher.api.util.logging.Markers
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.lwjgl.util.concurrent.InitialThread
import de.dasbabypixel.gamelauncher.lwjgl.util.logging.LWJGL
import de.dasbabypixel.gamelauncher.lwjgl.window.WrappingExecutorThread
import org.lwjgl.sdl.SDLError
import org.lwjgl.sdl.SDLEvents.*
import org.lwjgl.sdl.SDLInit.*
import org.lwjgl.sdl.SDL_Event
import org.lwjgl.sdl.SDL_EventFilter
import org.lwjgl.system.MemoryStack
import java.util.function.BooleanSupplier

object SDLThread : WrappingExecutorThread {
    override lateinit var handle: ExecutorThread
}

@OptIn(ExperimentalStdlibApi::class)
internal object SDLThreadImplementation : InitialThread.Implementation {
    private val memory: MemoryStack = MemoryStack.create(1024)
    private val logger = getLogger<SDLThreadImplementation>(Markers.LWJGL)
    override val name: String
        get() = "SDL-Thread"

    override fun initialized(thread: ExecutorThread): ExecutorThread {
        SDLThread.handle = thread
        return SDLThread
    }

    private val event: SDL_Event = SDL_Event.malloc(memory)
    private val windows = HashMap<Int, SDLWindow>()
    private lateinit var filter: SDL_EventFilter

    @Volatile
    private var initialized: Boolean = false

    @OptIn(ExperimentalStdlibApi::class)
    override fun startExecuting() {
        logger.debug("Initializing SDL")
        if (!SDL_Init(SDL_INIT_VIDEO or SDL_INIT_EVENTS)) {
            error("Failed to initialize SDL: " + SDLError.SDL_GetError())
        }
        SDL_SetAppMetadata("GameLauncher", "dev", "gamelauncher")

        filter = SDL_EventFilter.create { _, eventPtr ->
            try {
                val event = SDL_Event.create(eventPtr)
                when (event.type()) {
                    SDL_EVENT_WINDOW_CLOSE_REQUESTED -> {
                        assertOnSDLThread()
                        val windowEvent = event.window()
                        val window = windows[windowEvent.windowID()]!!
                        window.handleCloseRequested()
                    }

                    SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED -> run {
                        assertOnSDLThread()
                        val windowEvent = event.window()
                        val window = windows[windowEvent.windowID()] ?: return@run
                        val w = windowEvent.data1()
                        val h = windowEvent.data2()
                        window.handleFramebufferUpdate(w, h)
                    }

                    SDL_EVENT_MOUSE_MOTION -> {
                    }

                    SDL_EVENT_WINDOW_MOUSE_ENTER -> {
                    }

                    SDL_EVENT_WINDOW_MOUSE_LEAVE -> {
                    }

                    SDL_EVENT_WINDOW_EXPOSED -> {
                    }

                    SDL_EVENT_WINDOW_MOVED -> {
                    }

                    SDL_EVENT_WINDOW_RESIZED -> {
                    }

                    SDL_EVENT_WINDOW_SHOWN -> {
                    }

                    SDL_EVENT_WINDOW_HIDDEN -> {
                    }

                    SDL_EVENT_WINDOW_SAFE_AREA_CHANGED -> {
                    }

                    SDL_EVENT_QUIT -> {
                        println("Request quit")
                    }

                    else -> {
                        println("${currentThread.name}: ${event.type().toHexString()}")
                    }
                }
            } catch (t: Throwable) {
                GameLauncher.handleException(t)
            }
            true
        }
        SDL_AddEventWatch(filter, 0L)

        initialized = true
    }

    fun addWindow(window: SDLWindow) {
        assertOnSDLThread()
        windows[window.sdlWindowId] = window
    }

    fun removeWindow(window: SDLWindow) {
        assertOnSDLThread()
        windows.remove(window.sdlWindowId)
    }

    private fun assertOnSDLThread() {
        if (Debug.debug) {
            SDLThread.ensureOnThread()
        }
    }

    override fun workExecution() {
        while (true) {
            if (!SDL_PollEvent(event)) break
        }
    }

    override fun stopExecuting() {
        logger.debug("Terminating SDL")
        SDL_RemoveEventWatch(filter, 0L)
        filter.free()
        SDL_Quit()
    }

    override fun customSignal(): Boolean {
        postEmptyEvent()
        return true
    }

    private fun postEmptyEvent() {
        val event = SDL_Event.calloc().type(SDL_EVENT_USER)
        SDL_PushEvent(event)
        event.free()
    }

    override fun waitForSignal(superWait: Runnable) = superWait.run()

    override fun customAwait(): Boolean = true

    override fun awaitWork(superAwaitWork: BooleanSupplier): Boolean = error("unsupported")

    override fun customAwaitWork() {
        SDL_WaitEvent(null)
    }
}