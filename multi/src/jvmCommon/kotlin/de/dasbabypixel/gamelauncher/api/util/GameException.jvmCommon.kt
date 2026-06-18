package de.dasbabypixel.gamelauncher.api.util

import de.dasbabypixel.gamelauncher.api.util.stack.StackTrace

internal actual var GameException.internalMutableStacktrace: StackTrace
    get() = StackTrace((this as Exception).stackTrace, 0u)
    set(value) {
        (this as Exception).stackTrace =
            if (value.dropped == 0u) value.elements else value.elements.drop(value.dropped.toInt())
                .toTypedArray()
    }
