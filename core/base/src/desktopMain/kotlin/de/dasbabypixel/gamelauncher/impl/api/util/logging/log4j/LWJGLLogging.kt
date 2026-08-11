@file:OptIn(ExperimentalAtomicApi::class)

package de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j

import de.dasbabypixel.gamelauncher.lifecycle.ShutdownHandler
import de.dasbabypixel.gamelauncher.logging.CustomPatterns
import de.dasbabypixel.gamelauncher.logging.JvmLogging
import de.dasbabypixel.gamelauncher.logging.LogLevelRegistry
import de.dasbabypixel.gamelauncher.logging.LoggingPrintStream
import de.dasbabypixel.gamelauncher.logging.PatternRegistry
import de.dasbabypixel.gamelauncher.logging.getLogger
import de.dasbabypixel.gamelauncher.logging.withDefaultMarker
import de.dasbabypixel.gamelauncher.resource.SimpleResourceTracker
import de.dasbabypixel.gamelauncher.util.DesktopConfig
import de.dasbabypixel.gamelauncher.util.concurrent.AbstractThreadTask
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.util.concurrent.create
import de.dasbabypixel.gamelauncher.util.concurrent.sleep
import de.dasbabypixel.gamelauncher.util.debug.Debug
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

object LWJGLLogging {

    private val logger by getLogger()

    init {
        Logger.getLogger("org.jline").level = Level.ALL
    }

    fun init() {
        Log4jConfiguration.init()
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
        TerminalBuilder.builder().dumb(true).system(true).exec(false).encoding(JvmLogging.out.charset()).build()
    }

    private val reader: LineReader
    private var requestExit = AtomicBoolean(false)
    private var readerThread: Thread? = null

    init {
        val useAnsi = DesktopConfig.useAnsi()

        reader = LineReaderBuilder.builder().appName("GameLauncher").terminal(terminal).build()
        val patternRegistry = PatternRegistry(Log4jPatternPlatformProvider)
        LWJGLPatternProvider.register(patternRegistry)
        val customPatterns = CustomPatterns(patternRegistry)
        val logLevelRegistry = LogLevelRegistry(customPatterns)
        Log4jConfiguration.setup(patternRegistry, logLevelRegistry, customPatterns, useAnsi, reader)

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
            val stream = LoggingPrintStream(logger.withDefaultMarker("LWJGL"), printLocation = true)
            org.lwjgl.system.Configuration.DEBUG_STREAM.set(stream)
            if (APIUtil.DEBUG_STREAM != stream) error("Failed to inject debug logging into LWJGL")
        }
    }

    fun startReader() {
        readerThread = Thread.create(name = "Console Thread", taskFactory = { thread ->
            object : AbstractThreadTask(SimpleResourceTracker.global, thread) {
                val exitFuture = CompletableFuture<Unit>()
                val exit = AtomicBoolean(false)
                override fun run0() {
                    try {
                        while (!exit.load()) {
                            try {
                                if (requestExit.load()) throw UserInterruptException("")
                                val prompt = if (Debug.inIde) null else "Prompt: "
                                val line = reader.readLine(prompt)!!
                                if (line == "exit") {
                                    ShutdownHandler.shutdownGracefully()
                                    continue
                                }
                                logger.info("Read $line")
                            } catch (_: EndOfFileException) {
                            } catch (_: UserInterruptException) {
                                if (!exit.load()) {
                                    JvmLogging.out.println("User interrupted")
                                    Thread.sleep(1000)
                                    ShutdownHandler.shutdownGracefully()
                                }
                            } catch (t: Throwable) {
                                logger.error("Failed to read line, exiting", t)
                                ShutdownHandler.shutdownByError(t)
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
        }).also { it.start() }.thread
    }

    fun exit() {
        readerThread?.cleanupAsync()?.join()
        Log4jConfiguration.exit()
    }
}
