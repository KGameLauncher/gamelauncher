package de.dasbabypixel.gamelauncher.api.util.stack

import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.api.util.concurrent.currentThread

class StackTraceSnapshot(
    val stacktrace: StackTrace?, val cause: StackTraceSnapshot?, val thread: Thread
) {
    companion object {
        var calculateThreadStacks: Boolean = true
        fun new(): StackTraceSnapshot {
            val thread = currentThread
            val cause = cause(thread)
            if (calculateThreadStacks) {
                val stack = thread.stacktrace.drop(2)
                return StackTraceSnapshot(stack, cause, thread)
            }
            return StackTraceSnapshot(null, cause, thread)
        }

        private fun cause(thread: Thread): StackTraceSnapshot? {
            val task = thread.task
            if (task is CauseContainer) {
                return task.cause
            }
            return null
        }
    }

    fun buildCause(): Throwable {
        val cau = cause?.buildCause()
        val c = GameException("Thread ${thread.name}", cau)
        if (stacktrace != null) c.stacktrace = stacktrace
        else c.stacktrace = StackTrace(emptyArray())
        return c
    }

    override fun toString(): String {
        return "StackTraceSnapshot(stacktrace=$stacktrace, cause=$cause, thread=$thread)"
    }

    interface CauseContainer {
        val cause: StackTraceSnapshot?
    }
}