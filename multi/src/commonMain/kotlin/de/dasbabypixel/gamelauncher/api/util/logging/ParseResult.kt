package de.dasbabypixel.gamelauncher.api.util.logging

/**
 * Syntax works as follows:
 * ```
 * message = "msg"
 * thread = "main"
 * lsb = "%style{[}{#646464}"
 * rsb = "%style{]}{#646464}"
 * sb{content} = "%style{content}{#646464}
 *
 * "%message" -> "msg"
 *
 * "%lsb%thread%rsb: %message" -> "[main]: msg"
 * ```
 */
sealed interface ParseResult {
    val build: String

    fun simplify(): ParseResult

    fun printTree(builder: StringBuilder, level: Int = 0)

    val simplified: Boolean


    data object Empty : ParseResult {
        override val build: String = ""
        override fun simplify(): ParseResult = this
        override fun printTree(builder: StringBuilder, level: Int) {
            builder.append("[]")
        }

        override val simplified: Boolean = true
        override fun toString(): String = ""
    }

    data class Formatted(
        val pattern: CustomPattern,
        val content: ParseResult? = null,
        val options: ParseResult? = null
    ) : ParseResult {

        constructor(
            pattern: String, options: ParseResult? = null, content: ParseResult? = null
        ) : this(PatternRegistry.pattern(pattern), options, content)

        init {
            if (options != null && content == null) {
                throw PatternException("Bad pattern input: Options can't be nonnull with content null")
            }
        }

        fun assertContentEmpty() {
            if (content != null && content !is Empty) throw PatternException("Content for ${pattern.name} must be null or empty")
        }

        fun assertContentNull() {
            if (content != null) throw PatternException("Content for ${pattern.name} must be null")
        }

        fun assertOptionsNull() {
            if (options != null) throw PatternException("Options for ${pattern.name} must be null")
        }

        fun assertContentNotNull(): ParseResult {
            if (content == null) throw PatternException("Content for ${pattern.name} must not be null")
            return content
        }

        fun assertOptionsNotNull(): ParseResult {
            if (options == null) throw PatternException("Options for ${pattern.name} must not be null")
            return options
        }

        override fun simplify(): ParseResult {
            if (simplified) return this
            val maxDepth = 500
            var depth = 0
            var simple: ParseResult = pattern.simplifier.simplify(pattern, this)
            while (!simple.simplified) {
                depth++
                simple = simple.simplify()
                if (depth > maxDepth) {
                    val b = StringBuilder()
                    simple.printTree(b)
                    throw PatternException("Failed to simplify with depth 500: \n$b")
                }
            }
            return simple
        }

        override fun printTree(builder: StringBuilder, level: Int) {
            builder.append("%${pattern.name}")
            if (content != null) {
                builder.appendLine()
                builder.indent(level + 1).append("content=")
                content.printTree(builder, level + 1)
                if (options != null) {
                    builder.appendLine()
                    builder.indent(level + 1).append("options=")
                    options.printTree(builder, level + 1)
                }
            }
        }

        override val simplified: Boolean =
            pattern.simplifier == CustomPattern.NativeSimplifier && content?.simplified ?: true && options?.simplified ?: true

        override val build: String =
            "%${pattern.name}${content?.build?.let { it -> "{$it}${options?.build?.let { "{$it}" } ?: ""}" } ?: ""}"
    }

    data class Text(val text: String) : ParseResult {
        override val build: String = text
        override fun simplify(): ParseResult = this
        override fun printTree(builder: StringBuilder, level: Int) {
            builder.append('"').append(text).append('"')
        }

        override val simplified: Boolean = true
    }

    data class Multi(val results: List<ParseResult>) : ParseResult {
        constructor(vararg results: ParseResult) : this(results.toList())

        override val build: String = results.joinToString("", transform = { it.build })
        override fun simplify(): ParseResult = Multi(results.map { it.simplify() })
        override fun printTree(builder: StringBuilder, level: Int) {
            builder.appendLine("[")
            results.forEach {
                builder.indent(level + 1)
                it.printTree(builder, level + 1)
                builder.appendLine()
            }
            builder.indent(level).append("]")
        }

        override val simplified: Boolean = results.all { it.simplified }
    }
}

private fun StringBuilder.indent(level: Int): StringBuilder {
    return append("  ".repeat(level))
}
