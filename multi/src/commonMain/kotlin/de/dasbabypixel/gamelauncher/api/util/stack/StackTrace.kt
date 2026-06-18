package de.dasbabypixel.gamelauncher.api.util.stack

class StackTrace(val elements: Array<StackTraceElement>, val dropped: UInt) {
    fun drop(num: UInt): StackTrace = StackTrace(elements, dropped + num)
}
