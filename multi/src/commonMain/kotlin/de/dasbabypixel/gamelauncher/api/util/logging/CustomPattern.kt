package de.dasbabypixel.gamelauncher.api.util.logging

class CustomPattern(
    val name: String, val simplifier: Simplifier
) {
    fun interface Simplifier {
        fun simplify(pattern: CustomPattern, parse: ParseResult.Formatted): ParseResult
    }

    object NativeSimplifier : Simplifier {
        override fun simplify(pattern: CustomPattern, parse: ParseResult.Formatted): ParseResult {
            if (parse.simplified) return parse
            return ParseResult.Formatted(
                parse.pattern, parse.content?.simplify(), parse.options?.simplify()
            )
        }
    }

    override fun toString(): String = "%$name"

    interface PlatformProvider {
        fun pattern(name: String): CustomPattern

        fun isNative(name: String): Boolean

        fun freeze() {
            PatternRegistry.freeze()
        }
    }

    companion object
}

expect val CustomPattern.Companion.platform: CustomPattern.PlatformProvider
