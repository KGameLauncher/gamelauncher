package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.util.extension.toCallable
import de.dasbabypixel.gamelauncher.api.util.extension.toGameCallable
import de.dasbabypixel.gamelauncher.api.util.extension.toGameRunnable
import de.dasbabypixel.gamelauncher.api.util.function.GameCallable
import de.dasbabypixel.gamelauncher.api.util.function.GameRunnable
import de.dasbabypixel.gamelauncher.api.util.resource.GameResource
import de.dasbabypixel.gamelauncher.impl.provider.Providable
import de.dasbabypixel.gamelauncher.impl.provider.provide
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture

private val provider = provide<ThreadsProvider>()

sealed interface ThreadGroup {
    val name: String
    val parent: ThreadGroup?

    companion object {
        fun create(name: String): ThreadGroup = provider.createGroup(name)
        fun create(name: String, parent: ThreadGroup): ThreadGroup = provider.createGroup(name, parent)
    }

    interface Opened : ThreadGroup
}

val currentThread: Thread
    get() = Thread.currentThread

interface Thread : ThreadHolder, GameResource {
    companion object {
        val currentThread: Thread
            get() = provider.currentThread()

        fun park() {
            provider.park()
        }

        fun park(nanos: Long) {
            provider.park(nanos)
        }

        fun sleep(millis: Long) {
            provider.sleep(millis)
        }
    }

    val name: String
    val group: ThreadGroup
    val stackTrace: Array<StackTraceElement>
    override val thread: Thread
        get() = this

    fun start()
    fun unpark()

    fun ensureOnThread() {
        val thread = currentThread
        if (thread != this) {
            throw IllegalStateException("Wrong thread! Expected $name, was ${thread.name}")
        }
    }
}

interface ThreadsProvider : Providable {
    fun currentThread(): Thread
    fun park()
    fun park(nanos: Long)
    fun sleep(millis: Long)
    fun createGroup(name: String): ThreadGroup
    fun createGroup(name: String, parent: ThreadGroup): ThreadGroup
}

interface ThreadHolder {
    val thread: Thread
}

interface ExecutorThread : Thread, Executor

interface Executor {
    fun submit(runnable: Runnable): CompletableFuture<Unit> = submit(runnable.toGameRunnable())
    fun submit(runnable: GameRunnable): CompletableFuture<Unit> = submit(runnable.toCallable())

    fun submitR(runnable: Runnable) = submit(runnable)
    fun submitGR(runnable: GameRunnable) = submit(runnable)

    fun <T> submit(callable: Callable<T>): CompletableFuture<T> = submit(callable.toGameCallable())
    fun <T> submit(callable: GameCallable<T>): CompletableFuture<T>

    fun <T> submitC(callable: Callable<T>): CompletableFuture<T> = submit(callable)
    fun <T> submitGC(callable: GameCallable<T>): CompletableFuture<T> = submit(callable)
}
