package de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j

import de.dasbabypixel.gamelauncher.impl.api.util.logging.LogUse
import de.dasbabypixel.gamelauncher.impl.api.util.logging.slf4j.SLF4JLogger
import de.dasbabypixel.gamelauncher.logging.CustomPatterns
import de.dasbabypixel.gamelauncher.logging.JvmLogging
import de.dasbabypixel.gamelauncher.logging.LogLevelRegistry
import de.dasbabypixel.gamelauncher.logging.LogType
import de.dasbabypixel.gamelauncher.logging.LoggingPrintStream
import de.dasbabypixel.gamelauncher.logging.PatternParser
import de.dasbabypixel.gamelauncher.logging.PatternRegistry
import de.dasbabypixel.gamelauncher.logging.styleHex
import de.dasbabypixel.gamelauncher.util.Color
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.Core
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder
import org.apache.logging.log4j.core.config.builder.api.LoggableComponentBuilder
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder
import org.apache.logging.log4j.core.config.plugins.processor.PluginEntry
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry
import org.apache.logging.log4j.core.config.plugins.util.PluginType
import org.jline.reader.LineReader
import org.slf4j.LoggerFactory

object Log4jConfiguration {
    private fun ConfigurationBuilder<*>.layout(
        pattern: CharSequence, disableAnsi: Boolean
    ): LayoutComponentBuilder {
        val layout = newLayout("PatternLayout")
        layout.addAttribute("pattern", pattern)
        layout.addAttribute("alwaysWriteExceptions", true)
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
        logger: LoggableComponentBuilder<*>,
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

    private fun createConfiguration(
        logLevelRegistry: LogLevelRegistry,
        patternRegistry: PatternRegistry,
        customPatterns: CustomPatterns,
        useAnsi: Boolean,
        lineReader: LineReader
    ): Configuration {
        val terminalAppenderEntry = PluginEntry()
        terminalAppenderEntry.category = Core.CATEGORY_NAME
        terminalAppenderEntry.key = TerminalConsoleAppender.NAME.lowercase()
        terminalAppenderEntry.name = TerminalConsoleAppender.NAME
        terminalAppenderEntry.isPrintable = true
        terminalAppenderEntry.className = TerminalConsoleAppender::class.java.name
        val terminalAppenderType =
            PluginType(terminalAppenderEntry, TerminalConsoleAppender::class.java, Appender.ELEMENT_TYPE)
        PluginRegistry.getInstance().loadFromMainClassLoader()[Core.CATEGORY_NAME.lowercase()]!!.add(
            terminalAppenderType)
        TerminalConsoleAppender.lineReader = lineReader
        val disableAnsi = !useAnsi
        val builder = ConfigurationBuilderFactory.newConfigurationBuilder()

        val logTypeList = logLevelRegistry.levels()
        Level.values()
        val logger = builder.newRootLogger(Level.ALL, true)
        val stdout = builder.newLogger("stdout", Level.ALL, true).configureOut()
        val stderr = builder.newLogger("stderr", Level.ALL, true).configureOut()

//        builder.setStatusLevel(Level.WARN)

        fun LogType.Fixed.marker() = "Marker$marker"

        val markers: List<String> = logTypeList.filterIsInstance<LogType.Fixed>().map { it.marker() }

        val printStreamMismatchFilter = builder.filter("LevelMatchFilter",
            "level" to Log4jLevels.PRINT_STREAM.name(),
            onMatch = Filter.Result.DENY,
            onMismatch = Filter.Result.NEUTRAL)

        fun LoggableComponentBuilder<*>.add(name: String, streams: Boolean) {
            val nameFile = name + "File"

            val appenderRef = builder.newAppenderRef(name)
            val appenderRefFile = builder.newAppenderRef(nameFile)

            markers.map { marker ->
                builder.filter("MarkerFilter",
                    "marker" to marker,
                    onMatch = Filter.Result.DENY,
                    onMismatch = Filter.Result.NEUTRAL)
            }.run {
                if (!streams) plus(printStreamMismatchFilter) else this
            }.apply {
                if (isNotEmpty()) {
                    val filters = builder.newComponent("Filters")
                    forEach {
                        filters.addComponent(it)
                    }
                    appenderRef.addComponent(filters)
                    appenderRefFile.addComponent(filters)
                }
            }
            add(appenderRef)
            add(appenderRefFile)
        }

        logTypeList.forEach { logType ->
            val name = when (logType) {
                is LogType.Default -> "ROOT"
                is LogType.Stderr -> "stderr"
                is LogType.Stdout -> "stdout"
                is LogType.Fixed -> {
                    val marker = logType.marker()

                    val baseName = "Normal$marker"
                    val baseNameFile = "Normal${marker}File"
                    val streamName = "Stream$marker"
                    val streamNameFile = "Stream${marker}File"

                    val pattern = convertToPattern(customPatterns, logType, LogUse.NORMAL)
                    builder.terminalConsole(baseName, pattern, disableAnsi)
                    builder.file(baseNameFile, pattern)

                    val streamPattern = convertToPattern(customPatterns, logType, LogUse.STREAM)
                    builder.terminalConsole(streamName, streamPattern, disableAnsi)
                    builder.file(streamNameFile, streamPattern)

                    val acceptOnlyMarkerFilter =
                        builder.newFilter("MarkerFilter", Filter.Result.ACCEPT, Filter.Result.DENY)
                            .addAttribute("marker", marker)
                    logger.add(builder.newAppenderRef(baseName).add(acceptOnlyMarkerFilter))
                    logger.add(builder.newAppenderRef(baseNameFile).add(acceptOnlyMarkerFilter))

                    val streamFilters = builder.newComponent("Filters")
                        .addComponent(builder.newFilter("LevelMatchFilter", Filter.Result.ACCEPT, Filter.Result.DENY)
                            .addAttribute("level", Log4jLevels.PRINT_STREAM.name()))
                        .addComponent(builder.newFilter("MarkerFilter", Filter.Result.NEUTRAL, Filter.Result.DENY)
                            .addAttribute("marker", marker))
                    logger.add(builder.newAppenderRef(streamName).addComponent(streamFilters))
                    logger.add(builder.newAppenderRef(streamNameFile).addComponent(streamFilters))

//                    builder.appenderRef(logger, baseName, "MarkerFilter", "marker" to marker)
//                    builder.appenderRef(streamLogger,
//                        streamName,
//                        "MarkerFilter",
//                        "marker" to marker)
//                    builder.appenderRef(streamLogger,
//                        streamNameFile,
//                        "MarkerFilter",
//                        "marker" to marker)

                    return@forEach
                }
            }
            val nameFile = "${name}File"
            val pattern = convertToPattern(customPatterns, logType, LogUse.NORMAL)
            builder.terminalConsole(name, pattern, disableAnsi)
            builder.file(nameFile, pattern)
            when (logType) {
                is LogType.Default -> {
                    logger.add(name, false)
                }

                is LogType.Stderr -> {
                    stderr.add(builder.newAppenderRef(name))
                    stderr.add(builder.newAppenderRef(nameFile))
                }

                is LogType.Stdout -> {
                    stdout.add(builder.newAppenderRef(name))
                    stdout.add(builder.newAppenderRef(nameFile))
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

    private fun convertToPattern(
        customPatterns: CustomPatterns, logType: LogType, logUse: LogUse
    ): String {
        val pattern = logType.pattern
        val parse = PatternParser(customPatterns).parse(pattern)
        return Log4jPatternSerializer.serialize(customPatterns, parse, logUse)
    }

    fun setup(
        patternRegistry: PatternRegistry,
        logLevelRegistry: LogLevelRegistry,
        customPatterns: CustomPatterns,
        useAnsi: Boolean,
        lineReader: LineReader
    ) {
        LWJGLLogLevels.register(logLevelRegistry, customPatterns)
        val configuration = createConfiguration(logLevelRegistry, patternRegistry, customPatterns, useAnsi, lineReader)
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
    fun register(logLevelRegistry: LogLevelRegistry, customPatterns: CustomPatterns) {
        logLevelRegistry.registerLevel(LogType.Fixed("LWJGL", customPatterns.builtin.defaultPatternPrintWithLocation))
    }
}

object LWJGLPatternProvider {
    fun register(patternRegistry: PatternRegistry) {
        Log4jPatternPlatformProvider.register(patternRegistry)
        Log4jPatternPlatformProvider.freeze(patternRegistry)
    }
}
