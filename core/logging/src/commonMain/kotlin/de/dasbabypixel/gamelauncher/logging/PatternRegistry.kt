package de.dasbabypixel.gamelauncher.logging

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class PatternRegistry {
    private val frozen = AtomicBoolean(false)
    private val patterns = HashMap<String, CustomPattern>()

    fun registerPattern(platformProvider: CustomPattern.PlatformProvider, pattern: CustomPattern) {
        if (frozen.load()) throw IllegalStateException("PatternRegistry is frozen")
        if (pattern.simplifier == CustomPattern.NativeSimplifier && !platformProvider.isNative(
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
