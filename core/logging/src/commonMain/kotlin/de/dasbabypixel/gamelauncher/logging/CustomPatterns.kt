package de.dasbabypixel.gamelauncher.logging

import de.dasbabypixel.gamelauncher.util.Color

@Suppress("MemberVisibilityCanBePrivate", "unused")
class CustomPatterns(patternRegistry: PatternRegistry) {
    val colorTrace = Color(255, 0, 255).styleHex
    val colorDebug = Color(150, 150, 150).styleHex
    val colorInfo = Color(234, 218, 228).styleHex
    val colorWarn = Color(255, 255, 0).styleHex
    val colorError = Color(150, 0, 0).styleHex
    val colorFatal = Color(100, 0, 0).styleHex
    val colorStdout = Color(170, 170, 170).styleHex
    val colorStderr = Color(180, 0, 0).styleHex
    val colorThread = Color(200, 200, 200).styleHex
    val colorLogger = Color(0, 100, 255).styleHex
    val colorLocation = Color(150, 150, 150).styleHex
    val colorTime = Color(70, 255, 70).styleHex
    val colorGray = Color(100, 100, 100).styleHex
    val colorUnknown = Color(216, 0, 216).styleHex

    val style = patternRegistry.pattern("style")
    val time = patternRegistry.pattern("time")
    val level = patternRegistry.pattern("level")
    val logger = patternRegistry.pattern("logger")
    val thread = patternRegistry.pattern("thread")
    val highlight = patternRegistry.pattern("highlight")
    val exception = patternRegistry.pattern("exception")
    val marker = patternRegistry.pattern("marker")
    val gray = patternRegistry.pattern("gray")
    val sb = patternRegistry.pattern("sb")
    val msg = patternRegistry.pattern("msg")
    val lsb = patternRegistry.pattern("lsb")
    val rsb = patternRegistry.pattern("rsb")
    val location = patternRegistry.pattern("location")
    val newline = patternRegistry.pattern("n")
    val nativeMsg = patternRegistry.pattern("n_msg")
    val nativeHighlight = patternRegistry.pattern("n_highlight")
    val nativeTime = patternRegistry.pattern("n_time")
    val nativeLevel = patternRegistry.pattern("n_level")
    val nativeLogger = patternRegistry.pattern("n_logger")
    val nativeThread = patternRegistry.pattern("n_thread")
    val nativeException = patternRegistry.pattern("n_exception")
    val nativeMarker = patternRegistry.pattern("n_marker")
    val nativeLocation = patternRegistry.pattern("n_location")

    val builtin = Builtin()

    inner class Builtin {
        // Used for all loggers by default
        val defaultPattern = "$time $level $logger $thread: $msg$newline$exception"

        // Used for all loggers by default when using markers
        val defaultCustomPattern = "$time $level $marker $logger $thread: $msg$newline$exception"

        // Used for LoggingPrintStream
        val defaultPatternPrintWithLocation =
            "$time $logger $thread $location: $msg$newline$exception"
        val defaultPatternPrintWithoutLocation = "$time $logger $thread: $msg$newline$exception"

        // Used for LoggingPrintStream with Markers
        val defaultPatternPrintMarkerWithLocation =
            "$time $marker $logger $thread $location: $msg$newline$exception"
        val defaultPatternPrintMarkerWithoutLocation =
            "$time $marker $logger $thread: $msg$newline$exception"

        // Used for stdout
        val defaultStdoutPattern =
            "$time $sb{$style{STDOUT}{$colorStdout}} $thread $location $style{$nativeMsg}{$colorStdout}$newline$exception"

        // Used for stderr
        val defaultStderrPattern =
            "$time $sb{$style{STDERR}{$colorStderr}} $thread $location $style{$nativeMsg}{$colorStderr}$newline$exception"
    }
}

val Color.styleHex: String
    get() = "#$rgbHex"

