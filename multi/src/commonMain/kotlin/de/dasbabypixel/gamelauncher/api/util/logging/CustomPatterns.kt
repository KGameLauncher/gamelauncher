package de.dasbabypixel.gamelauncher.api.util.logging

import de.dasbabypixel.gamelauncher.api.util.Color

@Suppress("MemberVisibilityCanBePrivate", "unused")
object CustomPatterns {
    val C_TRACE = Color(255, 0, 255).styleHex
    val C_DEBUG = Color(150, 150, 150).styleHex
    val C_INFO = Color(234, 218, 228).styleHex
    val C_WARN = Color(255, 255, 0).styleHex
    val C_ERROR = Color(150, 0, 0).styleHex
    val C_FATAL = Color(100, 0, 0).styleHex
    val C_STDOUT = Color(170, 170, 170).styleHex
    val C_STDERR = Color(180, 0, 0).styleHex
    val C_THREAD = Color(200, 200, 200).styleHex
    val C_LOGGER = Color(0, 100, 255).styleHex
    val C_LOCATION = Color(150, 150, 150).styleHex
    val C_TIME = Color(70, 255, 70).styleHex
    val C_GRAY = Color(100, 100, 100).styleHex
    val C_UNKNOWN = Color(216, 0, 216).styleHex

    val STYLE = PatternRegistry.pattern("style")
    val TIME = PatternRegistry.pattern("time")
    val LEVEL = PatternRegistry.pattern("level")
    val LOGGER = PatternRegistry.pattern("logger")
    val THREAD = PatternRegistry.pattern("thread")
    val HIGHLIGHT = PatternRegistry.pattern("highlight")
    val EXCEPTION = PatternRegistry.pattern("exception")
    val MARKER = PatternRegistry.pattern("marker")
    val GRAY = PatternRegistry.pattern("gray")
    val SB = PatternRegistry.pattern("sb")
    val MSG = PatternRegistry.pattern("msg")
    val LSB = PatternRegistry.pattern("lsb")
    val RSB = PatternRegistry.pattern("rsb")
    val LOCATION = PatternRegistry.pattern("location")
    val NEWLINE = PatternRegistry.pattern("n")
    val N_MSG = PatternRegistry.pattern("n_msg")
    val N_HIGHLIGHT = PatternRegistry.pattern("n_highlight")
    val N_TIME = PatternRegistry.pattern("n_time")
    val N_LEVEL = PatternRegistry.pattern("n_level")
    val N_LOGGER = PatternRegistry.pattern("n_logger")
    val N_THREAD = PatternRegistry.pattern("n_thread")
    val N_EXCEPTION = PatternRegistry.pattern("n_exception")
    val N_MARKER = PatternRegistry.pattern("n_marker")
    val N_LOCATION = PatternRegistry.pattern("n_location")

    // Used for all loggers by default
    val DEFAULT_PATTERN = "$TIME $LEVEL $LOGGER $THREAD: $MSG$NEWLINE$EXCEPTION"

    // Used for all loggers by default when using markers
    val DEFAULT_CUSTOM_PATTERN = "$TIME $LEVEL $MARKER $LOGGER $THREAD: $MSG$NEWLINE$EXCEPTION"

    // Used for LoggingPrintStream
    val DEFAULT_PATTERN_PRINT_WITH_LOCATION =
        "$TIME $LOGGER $THREAD $LOCATION: $MSG$NEWLINE$EXCEPTION"
    val DEFAULT_PATTERN_PRINT_WITHOUT_LOCATION = "$TIME $LOGGER $THREAD: $MSG$NEWLINE$EXCEPTION"

    // Used for LoggingPrintStream with Markers
    val DEFAULT_PATTERN_PRINT_MARKER_WITH_LOCATION =
        "$TIME $MARKER $LOGGER $THREAD $LOCATION: $MSG$NEWLINE$EXCEPTION"
    val DEFAULT_PATTERN_PRINT_MARKER_WITHOUT_LOCATION =
        "$TIME $MARKER $LOGGER $THREAD: $MSG$NEWLINE$EXCEPTION"

    // Used for stdout
    val DEFAULT_STDOUT_PATTERN =
        "$TIME $SB{$STYLE{STDOUT}{$C_STDOUT}} $THREAD $LOCATION $STYLE{$N_MSG}{$C_STDOUT}$NEWLINE$EXCEPTION"

    // Used for stderr
    val DEFAULT_STDERR_PATTERN =
        "$TIME $SB{$STYLE{STDERR}{$C_STDERR}} $THREAD $LOCATION $STYLE{$N_MSG}{$C_STDERR}$NEWLINE$EXCEPTION"
}

val Color.styleHex: String
    get() = "#$rgbHex"

