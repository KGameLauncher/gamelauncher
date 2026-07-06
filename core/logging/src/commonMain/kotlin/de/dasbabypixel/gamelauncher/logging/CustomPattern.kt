package de.dasbabypixel.gamelauncher.logging

class CustomPattern(
    val name: String, val simplifier: Simplifier
) {
    fun interface Simplifier {
        fun simplify(
            patternRegistry: PatternRegistry, pattern: CustomPattern, parse: ParseResult.Formatted
        ): ParseResult
    }

    object NativeSimplifier : Simplifier {
        override fun simplify(
            patternRegistry: PatternRegistry, pattern: CustomPattern, parse: ParseResult.Formatted
        ): ParseResult {
            if (parse.simplified) return parse
            return ParseResult.Formatted(
                patternRegistry,
                parse.pattern,
                parse.content?.simplify(),
                parse.options?.simplify()
            )
        }
    }

    override fun toString(): String = "%$name"

    interface PlatformProvider {
        val patternNames: Collection<String>
        fun pattern(name: String): CustomPattern

        fun isNative(name: String): Boolean

        fun freeze(patternRegistry: PatternRegistry) {
            patternRegistry.freeze()
        }
    }

    companion object
}
