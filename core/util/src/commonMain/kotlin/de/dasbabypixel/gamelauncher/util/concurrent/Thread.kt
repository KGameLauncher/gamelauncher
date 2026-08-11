package de.dasbabypixel.gamelauncher.util.concurrent

import de.dasbabypixel.gamelauncher.resource.GameResource
import de.dasbabypixel.gamelauncher.util.stack.StackTrace

interface Thread : GameResource {
    companion object

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

val currentThread: Thread
    inline get() = Thread.currentThread

expect val Thread.Companion.currentThread: Thread
expect fun Thread.Companion.park()
expect fun Thread.Companion.park(nanos: Long)
expect fun Thread.Companion.sleep(millis: Long)
expect fun Thread.Companion.create(
    name: String,
    taskFactory: ThreadTaskFactory,
    daemon: Boolean = false,
    group: ThreadGroup = currentThread.group,
): Thread

inline fun <reified T : ThreadTask> Thread.Companion.create(
    name: String,
    crossinline taskFactory: (Thread) -> T,
    daemon: Boolean = false,
    group: ThreadGroup = currentThread.group
): T {
    return create(name, object : ThreadTaskFactory {
        override fun createTask(thread: Thread): ThreadTask {
            return taskFactory(thread)
        }
    }, daemon, group).task as T
}

expect inline fun Thread.Companion.dumpStack()
