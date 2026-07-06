package de.dasbabypixel.gamelauncher.util.concurrent

import de.dasbabypixel.gamelauncher.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.stack.StackTrace
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinWorkerThread
import java.util.concurrent.locks.LockSupport
import kotlin.concurrent.getOrSet
import java.lang.Thread as JThread
import java.lang.ThreadGroup as JThreadGroup

actual fun Thread.Companion.parkInternal() = LockSupport.park()
actual fun Thread.Companion.parkInternal(nanos: Long) = LockSupport.park(nanos)
actual fun Thread.Companion.sleepInternal(millis: Long) = JThread.sleep(millis)

@Suppress("NOTHING_TO_INLINE") // We inline in order to have a nicer dump
actual inline fun Thread.Companion.dumpStackInternal() = JThread.dumpStack()

private val currentThreadLocal: ThreadLocal<Thread> =
    ThreadLocal.withInitial {
        val thread = JThread.currentThread()
        if (thread is ThreadHolder) return@withInitial thread.thread
        null
    }

private class JThreadImpl(
    name: String, group: JThreadGroup, taskFactory: ThreadTaskFactory, daemon: Boolean
) : JThread(group, name), ThreadHolder {
    override val thread: ThreadImpl = ThreadImpl(this, taskFactory)

    override fun run() {
        thread.task.run()
    }

    init {
        if (daemon) isDaemon = true
    }
}

internal class ThreadImpl(val thread: JThread, threadTaskFactory: ThreadTaskFactory) : Thread {
    override var name: String
        get() = thread.name
        set(value) {
            thread.name = value
        }
    override val group: ThreadGroup = JThreadGroupCache[thread.threadGroup]
    override val stackTrace: StackTrace
        get() = StackTrace(thread.stackTrace, 2u)
    override val task = threadTaskFactory.createTask(this)

    override fun start() {
        task.start(thread::start)
    }

    override fun unpark() {
        LockSupport.unpark(thread)
    }

    override fun interrupt() {
        thread.interrupt()
    }

    override val cleanedUp: Boolean
        get() = task.cleanedUp
    override val cleanupFuture: CompletableFuture<Unit>
        get() = task.cleanupFuture
}

fun JThread.configureThirdPartyThread(
    threadTaskFactory: (Thread) -> ThreadTask, overwrite: Boolean = false
): Thread {
    return configureThirdPartyThread(object : ThreadTaskFactory {
        override fun createTask(thread: Thread): ThreadTask = threadTaskFactory(thread)
    }, overwrite)
}

fun JThread.configureThirdPartyThread(
    threadTaskFactory: ThreadTaskFactory = ExternalTaskFactory(false), overwrite: Boolean = false
): Thread {
    if (!overwrite && currentThreadLocal.get() != null) throw IllegalStateException("Already configured")
    val t = ThreadImpl(this, threadTaskFactory)
    currentThreadLocal.set(t)
    return t
}

actual fun Thread.Companion.createInternal(
    name: String, taskFactory: ThreadTaskFactory, daemon: Boolean, group: ThreadGroup
): Thread {
    return JThreadImpl(name, JThreadGroupCache[group], taskFactory, daemon).thread
}

internal class ExternalTaskFactory(val autoTrack: Boolean) : ThreadTaskFactory {
    private val tracker: ResourceTracker = ResourceTracker(true)
    override fun createTask(thread: Thread): ThreadTask =
        ExternalTask(autoTrack, tracker, thread)
}

private class ExternalTask(
    autoTrack: Boolean,
    tracker: ResourceTracker,
    override val thread: Thread
) : AbstractGameResource(tracker, autoTrack), ThreadTask {
    override fun cleanup0(): CompletableFuture<Unit> = error("Can't cleanup")

    override fun run() = error("Can't run")
    override fun start(internalStart: () -> Unit) = error("Can't start")
}

internal actual inline val Thread.Companion.currentThreadInternal: Thread
    get() {
        return currentThreadLocal.getOrSet {
            val thread = JThread.currentThread()
            if (thread is ForkJoinWorkerThread && thread.pool == ForkJoinPool.commonPool()) {
                // We want to support CompletableFuture async API so we want to support ForkJoinPool
                return@getOrSet ThreadImpl(thread, ExternalTaskFactory(false))
            }
            if (thread.isVirtual) {
                return@getOrSet ThreadImpl(thread, ExternalTaskFactory(false))
            }

            throw IllegalStateException("Current thread $thread is not a known thread")
        }
    }
