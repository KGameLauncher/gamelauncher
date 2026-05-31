package de.dasbabypixel.gamelauncher.api.util.logging.log4j

import de.dasbabypixel.gamelauncher.api.util.Color
import de.dasbabypixel.gamelauncher.api.util.logging.*
import de.dasbabypixel.gamelauncher.api.util.logging.slf4j.SLF4JLogger
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.Core
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.*
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration
import org.apache.logging.log4j.core.config.plugins.processor.PluginEntry
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry
import org.apache.logging.log4j.core.config.plugins.util.PluginType
import org.jline.reader.LineReader
import org.slf4j.LoggerFactory
import java.util.logging.LogManager
import kotlin.math.log

object Log4jConfiguration {
    private fun ConfigurationBuilder<*>.layout(
        pattern: CharSequence, disableAnsi: Boolean
    ): LayoutComponentBuilder {
        val layout = newLayout("PatternLayout")
        layout.addAttribute("pattern", pattern)
        layout.addAttribute("alwaysWriteExceptions", false)
        layout.addAttribute("disableAnsi", disableAnsi)
        return layout
    }

    private fun ConfigurationBuilder<*>.terminalConsole(
        name: String, pattern: String, disableAnsi: Boolean
    ) {
        val appender = newAppender(name, "TerminalConsole")
        val layout = layout(pattern, disableAnsi)
        appender.add(layout)
        add(appender)
    }

    private fun ConfigurationBuilder<*>.file(name: String, pattern: String) {
        val appender = newAppender(name, "File")
        appender.addAttribute("fileName", "latest.log")

        val layout = layout(pattern, disableAnsi = true)
        appender.add(layout)
        add(appender)
    }

    private fun ConfigurationBuilder<*>.filter(
        plugin: String,
        vararg attributes: Pair<String, String>,
        onMatch: Filter.Result = Filter.Result.ACCEPT,
        onMismatch: Filter.Result = Filter.Result.DENY
    ) = newFilter(plugin, onMatch, onMismatch).apply {
        attributes.forEach { addAttribute(it.first, it.second) }
    }

    private fun ConfigurationBuilder<*>.appenderRef(
        logger: RootLoggerComponentBuilder,
        ref: String,
        filterPlugin: String,
        vararg attributes: Pair<String, String>,
        onMatch: Filter.Result = Filter.Result.ACCEPT,
        onMismatch: Filter.Result = Filter.Result.DENY
    ) {
        val filter = filter(filterPlugin, attributes = attributes, onMatch, onMismatch)
        logger.add(newAppenderRef(ref).add(filter))
    }

    private fun LoggerComponentBuilder.configureOut(): LoggerComponentBuilder {
        return addAttribute("additivity", false)
    }

    private fun createConfiguration(useAnsi: Boolean, lineReader: LineReader): Configuration {
        val terminalAppenderEntry = PluginEntry()
        terminalAppenderEntry.category = Core.CATEGORY_NAME
        terminalAppenderEntry.key = TerminalConsoleAppender.NAME.lowercase()
        terminalAppenderEntry.name = TerminalConsoleAppender.NAME
        terminalAppenderEntry.isPrintable = true
        terminalAppenderEntry.className = TerminalConsoleAppender::class.java.name
        val terminalAppenderType = PluginType(
            terminalAppenderEntry, TerminalConsoleAppender::class.java, Appender.ELEMENT_TYPE
        )
        PluginRegistry.getInstance()
            .loadFromMainClassLoader()[Core.CATEGORY_NAME.lowercase()]!!.add(
            terminalAppenderType
        )
        TerminalConsoleAppender.lineReader = lineReader
        val disableAnsi = !useAnsi
        val builder = ConfigurationBuilderFactory.newConfigurationBuilder()

        val logTypeList = LogLevelRegistry.levels()
        val logger = builder.newAsyncRootLogger(Level.ALL)
        val stdout = builder.newAsyncLogger("stdout", Level.ALL).configureOut()
        val stderr = builder.newAsyncLogger("stderr", Level.ALL).configureOut()

        fun LogType.Fixed.marker() = marker

        val markers = logTypeList.filterIsInstance<LogType.Fixed>().map { it.marker() }
        logTypeList.forEach { logType ->
            val pattern = convertToPattern(logType)
            val name = when (logType) {
                LogType.Default -> "ROOT"
                LogType.Stderr -> "stderr"
                LogType.Stdout -> "stdout"
                is LogType.Fixed -> logType.marker()
            }
            val nameFile = name + "File"
            builder.terminalConsole("Base$name", pattern, disableAnsi)
            builder.file("Base$nameFile", pattern)
            when (logType) {
                LogType.Default -> {
                    builder.terminalConsole("Stream$name", pattern, disableAnsi)
                    builder.file("Stream$nameFile", pattern)
                    listOf(
                        "Stream" to builder.filter(
                            "LevelMatchFilter",
                            "level" to Log4jLevels.printStream.name(),
                            onMatch = Filter.Result.NEUTRAL,
                            onMismatch = Filter.Result.DENY
                        ), "Base" to builder.filter(
                            "LevelMatchFilter",
                            "level" to Log4jLevels.printStream.name(),
                            onMatch = Filter.Result.DENY,
                            onMismatch = Filter.Result.NEUTRAL
                        )
                    ).forEach { prefixAndFilter ->
                        val prefix = prefixAndFilter.first
                        val appenderRef: AppenderRefComponentBuilder =
                            builder.newAppenderRef(prefix + name)
                        val appenderRefFile = builder.newAppenderRef(prefix + nameFile)

                        markers.map { marker ->
                            builder.filter(
                                "MarkerFilter",
                                "marker" to marker,
                                onMatch = Filter.Result.DENY,
                                onMismatch = Filter.Result.NEUTRAL
                            )
                        }.plus(prefixAndFilter.second).apply {
                            if (isNotEmpty()) {
                                val filters = builder.newComponent("Filters")
                                forEach {
                                    filters.addComponent(it)
                                }
                                appenderRef.addComponent(filters)
                                appenderRefFile.addComponent(filters)
                            }
                        }
                        logger.add(appenderRef)
                        logger.add(appenderRefFile)
                    }
                }

                LogType.Stderr -> {
                    stderr.add(builder.newAppenderRef("Base$name"))
                    stderr.add(builder.newAppenderRef("Base$nameFile"))
                }

                LogType.Stdout -> {
                    stdout.add(builder.newAppenderRef("Base$name"))
                    stdout.add(builder.newAppenderRef("Base$nameFile"))
                }

                is LogType.Fixed -> {
                    builder.appenderRef(logger, "Base$name", "MarkerFilter", "marker" to name)
                    builder.appenderRef(logger, "Base$nameFile", "MarkerFilter", "marker" to name)
                }
            }
        }

//        builder.setPackages(TerminalConsoleAppender::class.java.packageName)
        builder.add(logger)
        builder.add(stdout)
        builder.add(stderr)

//        println(builder.toXmlConfiguration())

        val conf = builder.build(false)
        return conf
    }

    private fun convertToPattern(logType: LogType): String {
        val pattern = logType.pattern
        val parse = PatternParser.parse(pattern)
        return Log4jPatternSerializer.serialize(parse)
    }

    fun setup(useAnsi: Boolean, lineReader: LineReader) {
        LWJGLPatternProvider.register()
        LWJGLLogLevels.register()
        val configuration = createConfiguration(useAnsi, lineReader) as BuiltConfiguration
        Configurator.reconfigure(configuration)
        JvmLogging.init()
        System.setOut(LoggingPrintStream(SLF4JLogger(LoggerFactory.getLogger("stdout"))))
        System.setErr(LoggingPrintStream(SLF4JLogger(LoggerFactory.getLogger("stderr"))))
    }

    fun init() {
    }

    fun exit() {
        System.setOut(JvmLogging.out)
        System.setErr(JvmLogging.err)
    }

    init {
        Log4jLevels.init()
    }
}

object LWJGLLogLevels {
    private val FG_LWJGL = Color(0, 255, 255).styleHex
    fun register() {
        LogLevelRegistry.registerLevel(
            LogType.Fixed(
                "LWJGL", CustomPatterns.DEFAULT_PATTERN_PRINT_WITH_LOCATION
            )
        )
    }
}

object LWJGLPatternProvider {
    fun register() {
        Log4jPatternPlatformProvider.register()
        Log4jPatternPlatformProvider.freeze()
    }
}
