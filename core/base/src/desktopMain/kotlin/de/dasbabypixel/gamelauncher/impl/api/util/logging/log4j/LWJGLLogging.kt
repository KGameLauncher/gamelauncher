@file:OptIn(ExperimentalAtomicApi::class)

package de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.api.util.DesktopConfig
import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractThreadTask
import de.dasbabypixel.gamelauncher.api.util.debug.Debug
import de.dasbabypixel.gamelauncher.impl.api.util.logging.DesktopLoggingInstance
import de.dasbabypixel.gamelauncher.logging.JvmLogging
import de.dasbabypixel.gamelauncher.logging.LogLevelRegistry
import de.dasbabypixel.gamelauncher.logging.LoggingPrintStream
import de.dasbabypixel.gamelauncher.logging.getLogger
import de.dasbabypixel.gamelauncher.service.ServiceRegistry
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.lwjgl.system.APIUtil
import java.io.Console
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class LWJGLLogging(val tracker: ResourceTracker, val serviceRegistry: ServiceRegistry) {
    val loggingInstance = DesktopLoggingInstance()
    private val logger by getLogger(loggingInstance)

    init {
        Logger.getLogger("org.jline").level = Level.ALL

        JvmLogging.init()
    }

    private val console: Console? = System.console()

    // We always make sure to disable "exec" provider. It causes issues with "the pipe is being closed" when reading from stdin
    private val terminal: Terminal = if (console != null) {
        TerminalBuilder.builder().signalHandler {
            requestExit.store(true)
            JvmLogging.out.println("Receive signal ${it.name} - ${it.ordinal}")
        }.ffm(true).exec(false).apply {
            if (Debug.inIde) dumb(true).system(true)
            else system(true)
        }.encoding(console.charset()).build()
    } else {
        TerminalBuilder.builder().dumb(true).system(true).exec(false)
            .encoding(JvmLogging.out.charset()).build()
    }

    private val reader: LineReader
    private var requestExit = AtomicBoolean(false)
    private var consoleTask: ConsoleTask? = null
    private val logLevelRegistry = LogLevelRegistry(loggingInstance.customPatterns)
    private val configuration =
        Log4jConfiguration(logLevelRegistry, LWJGLLogLevels(logLevelRegistry), this)

    init {
        val useAnsi = DesktopConfig.useAnsi()

        reader = LineReaderBuilder.builder().appName("GameLauncher").terminal(terminal).build()
        configuration.setup(useAnsi, reader)

        reader.option(LineReader.Option.AUTO_GROUP, false)
        reader.option(LineReader.Option.AUTO_MENU_LIST, true)
        reader.option(LineReader.Option.AUTO_FRESH_LINE, true)
        reader.option(LineReader.Option.EMPTY_WORD_OPTIONS, false)
        reader.option(LineReader.Option.HISTORY_TIMESTAMPED, false)
        reader.option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)

        reader.variable(LineReader.BELL_STYLE, "none")
        reader.variable(LineReader.HISTORY_SIZE, 500)
        reader.variable(LineReader.HISTORY_FILE_SIZE, 2500)
        reader.variable(LineReader.COMPLETION_STYLE_LIST_BACKGROUND, "inverse")

        if (Debug.debug) {
            val stream = LoggingPrintStream(logger.withDefaultMarker("LWJGL"))
            org.lwjgl.system.Configuration.DEBUG_STREAM.set(stream)
            if (APIUtil.DEBUG_STREAM != stream) error("Failed to inject debug logging into LWJGL")
        }
    }

    fun startReader() {
        consoleTask =
            Thread.create(name = "Console Thread", taskFactory = ::ConsoleTask).also { it.start() }
    }

    fun exit() {
        consoleTask?.cleanupAsync()?.join()
        configuration.exit()
    }

    inner class ConsoleTask(thread: Thread) : AbstractThreadTask(loggingInstance, tracker, thread) {
        val exitFuture = CompletableFuture<Unit>()
        val exit = AtomicBoolean(false)
        override fun run0() {
            try {
                while (!exit.load()) {
                    try {
                        if (requestExit.compareAndSet(
                                expectedValue = true, newValue = false
                            )
                        ) throw UserInterruptException("")
                        val prompt = if (Debug.inIde) null else "Prompt: "
                        val line = reader.readLine(prompt)!!
                        if (line == "exit") {
                            serviceRegistry.singleInstance<GameLauncher>().shutdownGracefully()
                            continue
                        }
                        logger.info("Read $line")
                    } catch (_: EndOfFileException) {
                    } catch (_: UserInterruptException) {
                        if (!exit.load()) {
                            JvmLogging.out.println("User interrupted")
                            serviceRegistry.singleInstance<GameLauncher>().shutdownGracefully()
                            return
                        }
                    } catch (t: Throwable) {
                        logger.error("Failed to read line, exiting", t)
                        serviceRegistry.singleInstance<GameLauncher>().shutdownByError(t)
                    }
                }
            } finally {
                exitFuture.complete(Unit)
            }
        }

        override fun cleanup0(): CompletableFuture<Unit> {
            exit.store(true)
            thread.interrupt()
            return exitFuture
        }
    }
}
