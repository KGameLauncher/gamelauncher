package de.dasbabypixel.gamelauncher.api.util

import de.dasbabypixel.gamelauncher.api.util.stack.StackTrace

open class GameException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)

    var stacktrace: StackTrace = collectStackTrace()
}

internal expect fun GameException.collectStackTrace(): StackTrace
