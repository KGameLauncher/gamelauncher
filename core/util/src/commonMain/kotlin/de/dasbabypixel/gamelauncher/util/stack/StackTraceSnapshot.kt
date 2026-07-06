package de.dasbabypixel.gamelauncher.util.stack

import de.dasbabypixel.gamelauncher.util.GameException
import de.dasbabypixel.gamelauncher.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.util.concurrent.currentThread

class StackTraceSnapshot(
    val stacktrace: StackTrace?, val cause: StackTraceSnapshot?, val thread: Thread
) {
    companion object {
        var calculateThreadStacks: Boolean = true
        fun new(): StackTraceSnapshot {
            val thread = currentThread
            val cause = cause(thread)
            if (calculateThreadStacks) {
                val stack = thread.stackTrace.drop(2u)
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
        if (stacktrace != null) c.stackTrace = stacktrace
        else c.stackTrace = StackTrace(emptyArray(), 0u)
        return c
    }

    override fun toString(): String {
        return "StackTraceSnapshot(stacktrace=$stacktrace, cause=$cause, thread=$thread)"
    }

    interface CauseContainer {
        val cause: StackTraceSnapshot?
    }
}