package de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j

import de.dasbabypixel.gamelauncher.logging.CustomPattern
import de.dasbabypixel.gamelauncher.logging.ParseResult.Empty
import de.dasbabypixel.gamelauncher.logging.ParseResult.Formatted
import de.dasbabypixel.gamelauncher.logging.ParseResult.Multi
import de.dasbabypixel.gamelauncher.logging.ParseResult.Text
import de.dasbabypixel.gamelauncher.logging.PatternException
import de.dasbabypixel.gamelauncher.logging.PatternRegistry


@Suppress("MemberVisibilityCanBePrivate")
object Log4jPatternPlatformProvider : CustomPattern.PlatformProvider {
    private val patterns: MutableMap<String, CustomPattern> = HashMap()
    val patternNames: List<String>
        get() = patterns.keys.toList()

    fun addPattern(patternRegistry: PatternRegistry, name: String) {
        val pattern = CustomPattern(name, CustomPattern.NativeSimplifier)
        patterns[name] = pattern
        patternRegistry.registerPattern(pattern)
    }

    fun register(patternRegistry: PatternRegistry) {
        addPattern(patternRegistry, "style")
        addPattern(patternRegistry, "n_msg")
        addPattern(patternRegistry, "n_highlight")
        addPattern(patternRegistry, "n_time")
        addPattern(patternRegistry, "n_level")
        addPattern(patternRegistry, "n_logger")
        addPattern(patternRegistry, "n_thread")
        addPattern(patternRegistry, "n_exception")
        addPattern(patternRegistry, "n_marker")
        addPattern(patternRegistry, "n_location")
        addPattern(patternRegistry, "n")

        patternRegistry.registerPattern(CustomPattern("location") { c, _, parse ->
            parse.assertContentNull()
            parse.assertOptionsNull()
            Formatted(c, c.sb, Formatted(c, c.style, Formatted(c, c.nativeLocation), Text(c.colorLocation)))
        })
        patternRegistry.registerPattern(CustomPattern("msg") { c, _, parse ->
            parse.assertOptionsNull()
            val style = parse.content
            if (style != null) {
                Formatted(c, c.style, Formatted(c, c.nativeMsg), style)
            } else {
                Formatted(c, c.highlight, Formatted(c, c.nativeMsg))
            }
        })
        patternRegistry.registerPattern(CustomPattern("marker") { c, _, parse ->
            parse.assertOptionsNull()
            val style = parse.content ?: Text(c.colorLogger)
            Formatted(c, c.sb, Formatted(c, c.style, Formatted(c, c.nativeMarker), style))
        })
        patternRegistry.registerPattern(CustomPattern("exception") { c, _, parse ->
            parse.assertContentNull()
            parse.assertOptionsNull()
            Formatted(c, c.highlight, Formatted(c, c.nativeException))
        })
        patternRegistry.registerPattern(CustomPattern("highlight") { c, _, parse ->
            parse.assertContentNotNull()
            parse.assertOptionsNull()
            Formatted(c, c.nativeHighlight, parse.content)
        })
        patternRegistry.registerPattern(CustomPattern("time") { c, _, parse ->
            parse.assertContentNull()
            parse.assertOptionsNull()
            Formatted(c, c.sb, Formatted(c, c.style, Formatted(c, c.nativeTime), Text(c.colorTime)))
        })
        patternRegistry.registerPattern(CustomPattern("level") { c, _, parse ->
            parse.assertContentNull()
            parse.assertOptionsNull()
            Formatted(c, c.sb, Formatted(c, c.highlight, Formatted(c, c.nativeLevel)))
        })
        patternRegistry.registerPattern(CustomPattern("logger") { c, _, parse ->
            parse.assertContentNull()
            parse.assertOptionsNull()
            Formatted(c, c.sb, Formatted(c, c.style, Formatted(c, c.nativeLogger), Text(c.colorLogger)))
        })
        patternRegistry.registerPattern(CustomPattern("thread") { c, _, parse ->
            parse.assertContentNull()
            parse.assertOptionsNull()
            Formatted(c, c.sb, Formatted(c, c.style, Formatted(c, c.nativeThread), Text(c.colorThread)))
        })
        patternRegistry.registerPattern(CustomPattern("gray") { c, _, parse ->
            parse.assertContentNotNull()
            parse.assertOptionsNull()
            Formatted(c, c.style, parse.content, Text(c.colorGray))
        })
        patternRegistry.registerPattern(CustomPattern("sb") { c, _, parse ->
            parse.assertContentNotNull()
            parse.assertOptionsNull()
            Formatted(c, c.gray, Multi(Text("["), parse.content ?: Empty, Text("]")))
        })
        patternRegistry.registerPattern(CustomPattern("lsb") { c, _, parse ->
            parse.assertContentEmpty()
            Formatted(c, c.style, Text("["), parse.options ?: Text(c.colorGray))
        })
        patternRegistry.registerPattern(CustomPattern("rsb") { c, _, parse ->
            parse.assertContentEmpty()
            Formatted(c, c.style, Text("]"), parse.options ?: Text(c.colorGray))
        })
    }

    override fun pattern(name: String): CustomPattern {
        return patterns[name] ?: throw PatternException("No native pattern named $name")
    }

    override fun isNative(name: String): Boolean {
        return patterns.containsKey(name)
    }
}
