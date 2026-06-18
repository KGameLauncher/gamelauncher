package de.dasbabypixel.gamelauncher.api.util

import de.dasbabypixel.gamelauncher.api.util.stack.StackTrace

open class GameException : Exception {
    constructor(message: String? = null, cause: Throwable? = null) : super(message, cause)

    var stackTrace: StackTrace
        get() = internalMutableStacktrace
        set(value) {
            internalMutableStacktrace = value
        }
//
//    override fun fillInStackTrace(): Throwable? {
//        return this
//    }
}

internal expect var GameException.internalMutableStacktrace: StackTrace
