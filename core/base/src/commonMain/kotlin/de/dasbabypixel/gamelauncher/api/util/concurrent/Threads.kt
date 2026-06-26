package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.resource.GameResource
import de.dasbabypixel.gamelauncher.util.function.GameCallable
import de.dasbabypixel.gamelauncher.util.function.GameRunnable
import de.dasbabypixel.gamelauncher.util.stack.StackTrace

expect fun ThreadGroup.Companion.create(
    name: String, parent: ThreadGroup = currentThread.group
): ThreadGroup

interface ThreadGroup {
    val name: String
    val parent: ThreadGroup?

    companion object
}

val currentThread: Thread
    get() = Thread.currentThread

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

expect val ThreadGroup.Companion.root: ThreadGroup

interface ThreadTaskFactory {
    fun createTask(thread: Thread): ThreadTask
}

interface ThreadTask : GameResource {
    val thread: Thread

    fun run()

    fun start(internalStart: () -> Unit)

    fun start() = thread.start()
}

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

@Suppress("RedundantModalityModifier")
expect interface Executor {
    open fun submit(runnable: GameRunnable): CompletableFuture<Unit>
    open fun submitGR(runnable: GameRunnable): CompletableFuture<Unit>
    fun <T> submit(callable: GameCallable<T>): CompletableFuture<T>
    open fun <T> submitGC(callable: GameCallable<T>): CompletableFuture<T>
}
