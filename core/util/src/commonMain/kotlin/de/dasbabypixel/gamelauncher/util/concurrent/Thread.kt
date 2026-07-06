package de.dasbabypixel.gamelauncher.util.concurrent

import de.dasbabypixel.gamelauncher.util.resource.GameResource
import de.dasbabypixel.gamelauncher.util.stack.StackTrace

interface Thread : GameResource {
    companion object {
        val currentThread: Thread get() = currentThreadInternal
        fun sleep(millis: Long): Unit = sleepInternal(millis)
        fun park() = parkInternal()
        fun park(nanos: Long) = parkInternal(nanos)
        fun dumpStack() = dumpStackInternal()

        inline fun <reified T : ThreadTask> create(
            name: String,
            crossinline taskFactory: (Thread) -> T,
            daemon: Boolean = false,
            group: ThreadGroup = currentThread.group
        ): T {
            return createInternal(name, object : ThreadTaskFactory {
                override fun createTask(thread: Thread): ThreadTask {
                    return taskFactory(thread)
                }
            }, daemon, group).task as T
        }
    }

    var name: String
    val group: ThreadGroup
    val stackTrace: StackTrace
    val task: ThreadTask

    fun start()
    fun unpark()
    fun interrupt()

    fun ensureOnThread() {
        val thread = currentThread
        if (thread !== this) {
            throw IllegalStateException("Wrong thread! Expected $name, was ${thread.name}")
        }
    }
}

inline val currentThread: Thread
    get() = Thread.currentThread

internal expect inline val Thread.Companion.currentThreadInternal: Thread
internal expect fun Thread.Companion.parkInternal()
internal expect fun Thread.Companion.parkInternal(nanos: Long)
internal expect fun Thread.Companion.sleepInternal(millis: Long)
expect fun Thread.Companion.createInternal(
    name: String,
    taskFactory: ThreadTaskFactory,
    daemon: Boolean = false,
    group: ThreadGroup = currentThread.group,
): Thread

internal expect inline fun Thread.Companion.dumpStackInternal()
