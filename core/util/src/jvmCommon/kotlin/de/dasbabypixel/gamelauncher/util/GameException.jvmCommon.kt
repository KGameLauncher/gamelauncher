package de.dasbabypixel.gamelauncher.util

import de.dasbabypixel.gamelauncher.util.stack.StackTrace

internal actual var GameException.internalMutableStacktrace: StackTrace
    get() = StackTrace((this as Exception).stackTrace, 0u)
    set(value) {
        (this as Exception).stackTrace =
            if (value.dropped == 0u) value.elements else value.elements.drop(value.dropped.toInt())
                .toTypedArray()
    }
