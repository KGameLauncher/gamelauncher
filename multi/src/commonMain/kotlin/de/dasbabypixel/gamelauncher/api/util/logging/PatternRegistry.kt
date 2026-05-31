package de.dasbabypixel.gamelauncher.api.util.logging

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
object PatternRegistry {
    private val frozen = AtomicBoolean(false)
    private val patterns = HashMap<String, CustomPattern>()

    fun registerPattern(pattern: CustomPattern) {
        if (frozen.load()) throw IllegalStateException("PatternRegistry is frozen")
        if (pattern.simplifier == CustomPattern.NativeSimplifier && !CustomPattern.platform.isNative(
                pattern.name
            )
        ) {
            throw PatternException("Tried to illegally inject native pattern. This mustn't be done because of logging implementation limits")
        }
        patterns[pattern.name] = pattern
    }

    internal fun freeze() {
        frozen.store(true)
    }

    fun pattern(type: String): CustomPattern {
        return patterns[type] ?: throw PatternException("Pattern with type $type not found")
    }
}
