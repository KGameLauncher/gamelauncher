package de.dasbabypixel.gamelauncher.impl.api.util.logging.log4j

import de.dasbabypixel.gamelauncher.logging.CustomPattern
import de.dasbabypixel.gamelauncher.logging.CustomPatterns
import de.dasbabypixel.gamelauncher.logging.Formatted
import de.dasbabypixel.gamelauncher.logging.ParseResult.Empty
import de.dasbabypixel.gamelauncher.logging.ParseResult.Formatted
import de.dasbabypixel.gamelauncher.logging.ParseResult.Multi
import de.dasbabypixel.gamelauncher.logging.ParseResult.Text
import de.dasbabypixel.gamelauncher.logging.PatternException
import de.dasbabypixel.gamelauncher.logging.PatternRegistry

@Suppress("MemberVisibilityCanBePrivate")
class Log4jPatternPlatformProvider(
    val customPatterns: CustomPatterns
) : CustomPattern.PlatformProvider {
    private val patterns: MutableMap<String, CustomPattern> = HashMap()
    override val patternNames: List<String>
        get() = patterns.keys.toList()

    fun PatternRegistry.addPattern(name: String) {
        val pattern = CustomPattern(name, CustomPattern.NativeSimplifier)
        patterns[name] = pattern
        registerPattern(this@Log4jPatternPlatformProvider, pattern)
    }

    fun register(patternRegistry: PatternRegistry) {
        val pp = this
        patternRegistry.apply {
            addPattern("style")
            addPattern("n_msg")
            addPattern("n_highlight")
            addPattern("n_time")
            addPattern("n_level")
            addPattern("n_logger")
            addPattern("n_thread")
            addPattern("n_exception")
            addPattern("n_marker")
            addPattern("n_location")
            addPattern("n")
            customPatterns.apply {
                patternRegistry.registerPattern(
                    pp, CustomPattern("location") { registry, _, parse ->
                        parse.assertContentNull()
                        parse.assertOptionsNull()
                        Formatted(
                            registry, sb, Formatted(
                                registry, style, Formatted(nativeLocation), Text(colorLocation)
                            )
                        )
                    })
                patternRegistry.registerPattern(pp, CustomPattern("msg") { _, _, parse ->
                    parse.assertOptionsNull()
                    val st = parse.content
                    if (st != null) {
                        Formatted(
                            style, Formatted(nativeMsg), st
                        )
                    } else {
                        Formatted(

                            highlight, Formatted(nativeMsg)
                        )
                    }
                })
                patternRegistry.registerPattern(pp, CustomPattern("marker") { _, _, parse ->
                    parse.assertOptionsNull()
                    val st = parse.content ?: Text(colorLogger)
                    Formatted(
                        sb, Formatted(
                            style, Formatted(nativeMarker), st
                        )
                    )
                })
                patternRegistry.registerPattern(
                    pp, CustomPattern("exception") { _, _, parse ->
                        parse.assertContentNull()
                        parse.assertOptionsNull()
                        Formatted(highlight, Formatted(nativeException))
                    })
                patternRegistry.registerPattern(
                    pp, CustomPattern("highlight") { _, _, parse ->
                        parse.assertContentNotNull()
                        parse.assertOptionsNull()
                        Formatted(nativeHighlight, parse.content)
                    })
                patternRegistry.registerPattern(pp, CustomPattern("time") { _, _, parse ->
                    parse.assertContentNull()
                    parse.assertOptionsNull()
                    Formatted(
                        sb, Formatted(
                            style, Formatted(nativeTime), Text(colorTime)
                        )
                    )
                })
                patternRegistry.registerPattern(pp, CustomPattern("level") { _, _, parse ->
                    parse.assertContentNull()
                    parse.assertOptionsNull()
                    Formatted(sb, Formatted(highlight, Formatted(nativeLevel)))
                })
                patternRegistry.registerPattern(pp, CustomPattern("logger") { _, _, parse ->
                    parse.assertContentNull()
                    parse.assertOptionsNull()
                    Formatted(sb, Formatted(style, Formatted(nativeLogger), Text(colorLogger)))
                })
                patternRegistry.registerPattern(pp, CustomPattern("thread") { _, _, parse ->
                    parse.assertContentNull()
                    parse.assertOptionsNull()
                    Formatted(sb, Formatted(style, Formatted(nativeThread), Text(colorThread)))
                })
                patternRegistry.registerPattern(pp, CustomPattern("gray") { _, _, parse ->
                    parse.assertContentNotNull()
                    parse.assertOptionsNull()
                    Formatted(style, parse.content, Text(colorGray))
                })
                patternRegistry.registerPattern(pp, CustomPattern("sb") { _, _, parse ->
                    parse.assertContentNotNull()
                    parse.assertOptionsNull()
                    Formatted(gray, Multi(Text("["), parse.content ?: Empty, Text("]")))
                })
                patternRegistry.registerPattern(pp, CustomPattern("lsb") { _, _, parse ->
                    parse.assertContentEmpty()
                    Formatted(style, Text("["), parse.options ?: Text(colorGray))
                })
                patternRegistry.registerPattern(pp, CustomPattern("rsb") { _, _, parse ->
                    parse.assertContentEmpty()
                    Formatted(style, Text("]"), parse.options ?: Text(colorGray))
                })
            }
        }
    }

    override fun pattern(name: String): CustomPattern {
        return patterns[name] ?: throw PatternException("No native pattern named $name")
    }

    override fun isNative(name: String): Boolean {
        return patterns.containsKey(name)
    }
}
