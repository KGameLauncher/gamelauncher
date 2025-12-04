package de.dasbabypixel.gamelauncher.lwjgl

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractThread
import de.dasbabypixel.gamelauncher.api.util.logging.Logging
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.impl.provider.registerProvider
import de.dasbabypixel.gamelauncher.impl.util.concurrent.ThreadGroupCache
import de.dasbabypixel.gamelauncher.lwjgl.opengl.LWJGLGLES
import de.dasbabypixel.gamelauncher.lwjgl.util.concurrent.DisruptorMPSC
import de.dasbabypixel.gamelauncher.lwjgl.util.concurrent.InitialThread
import de.dasbabypixel.gamelauncher.lwjgl.window.DoubleBufferedAsyncRenderImpl
import de.dasbabypixel.gamelauncher.lwjgl.window.WindowSystem
import de.dasbabypixel.gamelauncher.opengl.ProvidableGL
import org.jline.jansi.Ansi
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.system.exitProcess

val startInitialThreadLatch = CountDownLatch(1)
val startLauncherLatch = CountDownLatch(1)
fun main() {
    Logging.out.print(Ansi.ansi().eraseScreen())
    Logging.out.flush()
    val latch = CountDownLatch(1)
    thread(name = "InfinitySleeper", isDaemon = true, priority = Thread.MAX_PRIORITY) {
        latch.countDown()
        Thread.sleep(9223372036854775783)
    }
    latch.await()

    @Suppress("UnusedExpression")
    DisruptorMPSC // Init Disruptor. This is required for our threads

    val start = Runnable {
        LWJGLGameLauncher()
        GameLauncher.start()
    }

    // Some windowing systems need to be run on the application's main thread
    val implementation = WindowSystem.windowSystem.initThread()
    if (implementation != null) {
        InitialThread.init(implementation, {
            object : AbstractThread(ThreadGroupCache[Thread.currentThread().threadGroup], "entrypoint") {
                override fun run() {
                    try {
                        WindowSystem.windowSystem.track()
                        start.run()
                        cleanupAsync().join()
                    } catch (t: Throwable) {
                        logger.error("Failed to run GameLauncher", t)
                        exitProcess(0)
                    }
                }

                override fun cleanup0(): CompletableFuture<Unit>? = null
            }.start()
            startInitialThreadLatch.await()
        }, {
            startLauncherLatch.countDown()
        })
    } else {
        WindowSystem.windowSystem.track()
        start.run()
    }
}

fun started() {
    startInitialThreadLatch.countDown()
    startLauncherLatch.await()
    val logger = getLogger<Main>()
    logger.info("In IDE: {}", Debug.inIde)

    ProvidableGL(LWJGLGLES).registerProvider("gles")
//    GLFWThread.start()

    val count = AtomicInteger(1)
    for (i in 0 until count.get()) {
        val window = WindowSystem.windowSystem.createWindow()
        var closed = false
        window.requestCloseCallback {
            if (closed) return@requestCloseCallback
            closed = true
            window.cleanupAsync().thenRun {
                if (count.decrementAndGet() == 0) {
                    try {
                        GameLauncher.stop()
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }
        }
        window.forceFocus()
        window.frameSync.updateFramerate(0)
        Thread.startVirtualThread {
            while (true) {
                println(window.frameSync.frameCount.getAndSet(0))
                Thread.sleep(1000)
            }
        }
        window.changeRenderImplementation(DoubleBufferedAsyncRenderImpl())
        window.show().thenRun {
            println("shown")
        }
    }
}

fun stopped() {
//    window.cleanup().join()
    WindowSystem.windowSystem.cleanupAsync().join()
    InitialThread.cleanup()
}

class Main