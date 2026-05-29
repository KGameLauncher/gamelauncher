package de.dasbabypixel.gamelauncher.api.util

import de.dasbabypixel.gamelauncher.api.util.stack.StackTrace

internal actual fun GameException.collectStackTrace(): StackTrace {
    return buildStackTrace((this as java.lang.Exception).stackTrace)
}

fun buildStackTrace(elements: Array<StackTraceElement>): StackTrace {
    return StackTrace(elements.map { de.dasbabypixel.gamelauncher.api.util.stack.StackTraceElement() }.toTypedArray())
}
