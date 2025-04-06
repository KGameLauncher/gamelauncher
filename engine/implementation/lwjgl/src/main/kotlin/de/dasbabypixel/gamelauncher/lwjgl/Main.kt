package de.dasbabypixel.gamelauncher.lwjgl

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractThread
import de.dasbabypixel.gamelauncher.api.util.logging.Logging
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.impl.util.concurrent.ThreadGroupCache
import de.dasbabypixel.gamelauncher.lwjgl.util.concurrent.DisruptorMPSC
import de.dasbabypixel.gamelauncher.lwjgl.util.concurrent.InitialThread
import de.dasbabypixel.gamelauncher.lwjgl.window.DoubleBufferedAsyncRenderImpl
import de.dasbabypixel.gamelauncher.lwjgl.window.LWJGLWindow
import de.dasbabypixel.gamelauncher.lwjgl.window.WindowSystem
import org.jline.jansi.Ansi
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

val startInitialThreadLatch = CountDownLatch(1)
val startLauncherLatch = CountDownLatch(1)
fun main(args: Array<String>) {
    Logging.out.print(Ansi.ansi().eraseScreen())
    Logging.out.flush()
    val latch = CountDownLatch(1)
    thread(isDaemon = true, priority = Thread.MAX_PRIORITY) {
        latch.countDown()
        Thread.sleep(9223372036854775783)
    }
    latch.await()

    DisruptorMPSC // Init Disruptor. This is required for our threads

    val start = Runnable {
        LWJGLGameLauncher()
        GameLauncher.start()
    }

    val implementation = WindowSystem.windowSystem.initThread()
    if (implementation != null) {
        InitialThread.init(implementation, {
            object : AbstractThread(ThreadGroupCache[Thread.currentThread().threadGroup], "entrypoint") {
                override fun run() {
                    start.run()
                    cleanup()
                }

                override fun cleanup0(): CompletableFuture<Unit>? = null
            }.start()
            startInitialThreadLatch.await()
        }, {
            startLauncherLatch.countDown()
        })
    } else {
        start.run()
    }
}

lateinit var window: LWJGLWindow

fun started() {
    startInitialThreadLatch.countDown()
    startLauncherLatch.await()
    val logger = getLogger<Main>()
    logger.info("In IDE: {}", Debug.inIde)

//    GLFWThread.start()

    window = WindowSystem.windowSystem.createWindow()
    window.requestCloseCallback { GameLauncher.stop() }
//    window.frameSync.updateFramerate(30)
    window.changeRenderImplementation(DoubleBufferedAsyncRenderImpl())
    window.show().thenRun {
        println("shown")
    }
}

fun stopped() {
    window.cleanup().join()
    InitialThread.cleanup()
}

class Main