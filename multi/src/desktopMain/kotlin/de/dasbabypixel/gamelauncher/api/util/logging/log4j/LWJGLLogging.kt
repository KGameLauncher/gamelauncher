package de.dasbabypixel.gamelauncher.api.util.logging.log4j

import de.dasbabypixel.gamelauncher.api.lifecycle.ShutdownHandler
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.Config
import de.dasbabypixel.gamelauncher.api.util.DesktopConfig
import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractThreadTask
import de.dasbabypixel.gamelauncher.api.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadTask
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadTaskFactory
import de.dasbabypixel.gamelauncher.api.util.concurrent.create
import de.dasbabypixel.gamelauncher.api.util.concurrent.dumpStack
import de.dasbabypixel.gamelauncher.api.util.concurrent.sleep
import de.dasbabypixel.gamelauncher.api.util.debug.Debug
import de.dasbabypixel.gamelauncher.api.util.logging.JvmLogging
import de.dasbabypixel.gamelauncher.api.util.logging.LoggingPrintStream
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import java.util.logging.Level
import java.util.logging.Logger

class LWJGLLogging {
    companion object {
        private val reader: LineReader
        private var requestExit: Boolean = false

        init {
            val useAnsi = DesktopConfig.useAnsi()
            Logger.getLogger("org.jline").level = Level.ALL

            val console = System.console()
            val terminal: Terminal
            // We always make sure to disable "exec" provider. It causes issues with "the pipe is being closed" when reading from stdin
            if (console != null) {
                terminal = TerminalBuilder.builder().signalHandler {
                    JvmLogging.out.println("Receive signal ${it.name} - ${it.ordinal}")
                    requestExit = true
                }.ffm(true).exec(false).apply {
                    if (Debug.inIde) dumb(true).system(true)
                    else system(true)
                    System.console()
                }.encoding(console.charset()).build()
            } else {
                terminal = TerminalBuilder.builder().dumb(true).system(true).exec(false)
                    .encoding(JvmLogging.out.charset()).build()
            }

            reader = LineReaderBuilder.builder().appName("GameLauncher").terminal(terminal).build()
            Log4jConfiguration.setup(useAnsi, reader)

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
                val stream = LoggingPrintStream(
                    getLogger<LWJGLLogging>("LWJGL"), printLocation = true
                )
                stream.println("ABC")
                // TODO
//                org.lwjgl.system.Configuration.DEBUG_STREAM.set(stream)
//                if (APIUtil.DEBUG_STREAM != stream) error("Failed to inject debug logging into LWJGL")
            }
        }

        fun init() {
            Log4jConfiguration.init()
        }

        fun startReader() {

            val logger = getLogger<LWJGLLogging>()

            val thread =
                Thread.create(name = "Console Thread", taskFactory = object : ThreadTaskFactory {
                    override fun createTask(thread: Thread): ThreadTask {
                        return object : AbstractThreadTask(ResourceTracker.global, thread) {
                            override fun run0() {
                                while (true) {
                                    try {
                                        if (requestExit) throw UserInterruptException("")
                                        val prompt = if (Debug.inIde) null else "Prompt: "
                                        val line = reader.readLine(prompt) ?: break
                                        if (line == "exit") ShutdownHandler.shutdownGracefully()
                                        logger.info("Read $line")
                                    } catch (_: EndOfFileException) {
                                    } catch (_: UserInterruptException) {
                                        JvmLogging.out.println("Interrupted")
                                        Thread.sleep(1000)
                                        ShutdownHandler.shutdownGracefully()
                                    } catch (t: Throwable) {
                                        logger.error("Failed to read line, exiting", t)
                                        ShutdownHandler.shutdownByError(t)
                                    }
                                }
                            }

                            override fun cleanup0(): de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture<Unit> =
                                error("Can't cleanup")
                        }
                    }
                })
        }

        fun exit() {
            Log4jConfiguration.exit()
        }
    }
}
