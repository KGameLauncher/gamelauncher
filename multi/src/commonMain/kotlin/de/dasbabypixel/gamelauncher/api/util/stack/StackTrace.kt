package de.dasbabypixel.gamelauncher.api.util.stack

class StackTrace(val elements: Array<StackTraceElement>) {
    fun drop(num: Int): StackTrace = StackTrace(elements.drop(num).toTypedArray())
}
