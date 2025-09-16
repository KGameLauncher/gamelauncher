package de.dasbabypixel.gamelauncher.lwjgl.util.logging

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.api.config.Config
import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.api.util.logging.Logging
import de.dasbabypixel.gamelauncher.api.util.logging.Markers
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.impl.util.logging.LoggingPrintStream
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.lwjgl.system.Configuration
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import java.util.logging.Logger

class LWJGLLogging {
    companion object {
        val threadGroup = ThreadGroup.create("lwjgl-logging")
        fun init() {
            val useAnsi = Config.USE_ANSI.value
            Logger.getLogger("org.jline").level = Level.ALL

            var requestExit = false
            val console = System.console()
            val terminal: Terminal
            // We always make sure to disable "exec" provider. It causes issues with "the pipe is being closed" when reading from stdin
            if (console != null) {
                terminal = TerminalBuilder.builder().signalHandler {
                    Logging.out.println("Receive signal ${it.name} - ${it.ordinal}")
                    requestExit = true
                }.ffm(true).exec(false).apply {
                    if (Debug.inIde) dumb(true).system(true)
                    else system(true)
                    System.console()
                }.encoding(console.charset()).build()
            } else {
                terminal = TerminalBuilder.builder().dumb(true).system(true).exec(false).encoding(Logging.out.charset())
                    .build()
            }

            val reader = LineReaderBuilder.builder().appName("GameLauncher").terminal(terminal).build()
            Log4jConfiguration.setup(useAnsi, reader)

            if (Debug.debug) {
                val stream = LoggingPrintStream(getLogger<LWJGLLogging>(Markers.LWJGL), printLocation = false)
                Configuration.DEBUG_STREAM.set(stream)
            }

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

            val logger = getLogger<LWJGLLogging>()

            object : AbstractThread(threadGroup, "Console Thread") {
                override fun run() {
                    while (true) {
                        try {
                            if (requestExit) throw UserInterruptException("")
                            val prompt = if (Debug.inIde) null else "Prompt: "
                            val line = reader.readLine(prompt) ?: break
                            if (line == "exit") GameLauncher.stop()
                            logger.info("Read $line")
                        } catch (_: EndOfFileException) {
                        } catch (_: UserInterruptException) {
                            Logging.out.println("Interrupted")
                            Thread.sleep(1000)
                            GameLauncher.stop()
                        } catch (t: Throwable) {
                            logger.error("Failed to read line, exiting", t)
                            GameLauncher.handleException(t)
                        }
                    }
                }

                override fun cleanup0(): CompletableFuture<Unit>? = null
            }
        }

        fun exit() {
            Log4jConfiguration.exit()
        }
    }
}
