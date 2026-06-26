package de.dasbabypixel.gamelauncher.logging

sealed interface LogType {
    val pattern: String

    class Stdout(customPatterns: CustomPatterns) : LogType {
        override val pattern: String = customPatterns.builtin.defaultStdoutPattern
    }

    class Stderr(customPatterns: CustomPatterns) : LogType {
        override val pattern: String = customPatterns.builtin.defaultStderrPattern
    }

    /**
     * Default LogType for all markers. Can be overridden with another LogType on a per-marker basis.
     */
    class Default(customPatterns: CustomPatterns) : LogType {
        override val pattern: String = customPatterns.builtin.defaultPattern
    }

    data class Fixed(val marker: String, override val pattern: String) : LogType
}
