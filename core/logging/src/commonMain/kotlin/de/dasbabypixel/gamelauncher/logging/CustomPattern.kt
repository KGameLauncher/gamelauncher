package de.dasbabypixel.gamelauncher.logging

class CustomPattern(
    val name: String, val simplifier: Simplifier
) {
    fun interface Simplifier {
        fun simplify(
            customPatterns: CustomPatterns, pattern: CustomPattern, parse: ParseResult.Formatted
        ): ParseResult
    }

    object NativeSimplifier : Simplifier {
        override fun simplify(
            customPatterns: CustomPatterns, pattern: CustomPattern, parse: ParseResult.Formatted
        ): ParseResult {
            if (parse.simplified) return parse
            return ParseResult.Formatted(customPatterns,
                parse.pattern,
                parse.content?.simplify(),
                parse.options?.simplify())
        }
    }

    override fun toString(): String = "%$name"

    interface PlatformProvider {
        fun pattern(name: String): CustomPattern

        fun isNative(name: String): Boolean

        fun freeze(patternRegistry: PatternRegistry) {
            patternRegistry.freeze()
        }
    }

    companion object
}
