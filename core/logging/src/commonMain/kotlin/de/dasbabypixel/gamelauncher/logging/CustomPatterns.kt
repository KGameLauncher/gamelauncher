package de.dasbabypixel.gamelauncher.logging

import de.dasbabypixel.gamelauncher.util.Color

@Suppress("MemberVisibilityCanBePrivate", "unused")
class CustomPatterns(val patternRegistry: PatternRegistry) {
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

    private fun pattern(type: String) = lazy { patternRegistry.pattern(type) }

    val style by pattern("style")
    val time by pattern("time")
    val level by pattern("level")
    val logger by pattern("logger")
    val thread by pattern("thread")
    val highlight by pattern("highlight")
    val exception by pattern("exception")
    val marker by pattern("marker")
    val gray by pattern("gray")
    val sb by pattern("sb")
    val msg by pattern("msg")
    val lsb by pattern("lsb")
    val rsb by pattern("rsb")
    val location by pattern("location")
    val newline by pattern("n")
    val nativeMsg by pattern("n_msg")
    val nativeHighlight by pattern("n_highlight")
    val nativeTime by pattern("n_time")
    val nativeLevel by pattern("n_level")
    val nativeLogger by pattern("n_logger")
    val nativeThread by pattern("n_thread")
    val nativeException by pattern("n_exception")
    val nativeMarker by pattern("n_marker")
    val nativeLocation by pattern("n_location")

    val builtin by lazy { Builtin() }

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

