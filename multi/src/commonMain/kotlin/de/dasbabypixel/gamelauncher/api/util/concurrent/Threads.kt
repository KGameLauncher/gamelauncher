package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.resource.GameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.function.GameCallable
import de.dasbabypixel.gamelauncher.api.util.function.GameRunnable
import de.dasbabypixel.gamelauncher.api.util.function.toCallable
import de.dasbabypixel.gamelauncher.api.util.stack.StackTrace
import de.dasbabypixel.gamelauncher.impl.Providable

expect fun ThreadGroup.Companion.create(
    name: String,
    parent: ThreadGroup = currentThread.group
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
expect inline fun Thread.Companion.dumpStack()

interface ThreadTaskFactory {
    fun createTask(thread: Thread): ThreadTask
}

interface ThreadTask : GameResource {
    fun run()
}

interface Thread : GameResource {
    companion object

    val name: String
    val group: ThreadGroup
    val stacktrace: StackTrace
    val task: ThreadTask

    fun start()
    fun unpark()

    fun ensureOnThread() {
        val thread = currentThread
        if (thread !== this) {
            throw IllegalStateException("Wrong thread! Expected $name, was ${thread.name}")
        }
    }
}

interface Executor {
    fun submit(runnable: GameRunnable): CompletableFuture<Unit> = submit(runnable.toCallable())
    fun submitGR(runnable: GameRunnable) = submit(runnable)
    fun <T> submit(callable: GameCallable<T>): CompletableFuture<T>
    fun <T> submitGC(callable: GameCallable<T>): CompletableFuture<T> = submit(callable)
}
