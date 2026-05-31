package de.dasbabypixel.gamelauncher.api.util.logging

sealed interface LogType {
    val pattern: String

    object Stdout : LogType {
        override val pattern: String = CustomPatterns.DEFAULT_STDOUT_PATTERN
    }

    object Stderr : LogType {
        override val pattern: String = CustomPatterns.DEFAULT_STDERR_PATTERN
    }

    /**
     * Default LogType for all markers. Can be overridden with another LogType on a per-marker basis.
     */
    object Default : LogType {
        override val pattern: String = CustomPatterns.DEFAULT_PATTERN
    }

    data class Fixed(val marker: String, override val pattern: String) : LogType
}

sealed class LogType2(
    val marker: String
) {
    class Colored(marker: String, val fgStyle: String? = null, val bgStyle: String? = null) :
        LogType2(marker)

    class Pattern(marker: String, val pattern: String) : LogType2(marker)
    class Custom(marker: String) : LogType2(marker)
    data object Stdout : LogType2("")
    data object Stderr : LogType2("")
    data object Root : LogType2("")

    override fun toString(): String = marker
}
