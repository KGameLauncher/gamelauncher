package de.dasbabypixel.gamelauncher.util.concurrent

import de.dasbabypixel.gamelauncher.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.resource.DisabledResourceTracker
import de.dasbabypixel.gamelauncher.util.stack.StackTrace
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinWorkerThread
import java.util.concurrent.locks.LockSupport
import kotlin.concurrent.getOrSet
import java.lang.Thread as JThread
import java.lang.ThreadGroup as JThreadGroup

internal object JThreadGroupCache {
    private val map = WeakHashMap<JThreadGroup, ThreadGroup>()
    operator fun get(group: JThreadGroup): ThreadGroup {
        synchronized(map) {
            return map.computeIfAbsent(group) { JCommonThreadGroup(it) }
        }
    }

    operator fun get(group: ThreadGroup): JThreadGroup {
        return (group as JCommonThreadGroup).group
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

    override fun cleanupAsync(): CompletableFuture<Unit> = task.cleanupAsync()
}

private object ExternalTaskFactory : ThreadTaskFactory {
    override fun createTask(thread: Thread): ThreadTask = ExternalTask(thread)
}

fun JThread.configureThirdPartyThread(
    threadTaskFactory: (Thread) -> ThreadTask, overwrite: Boolean = false
): Thread {
    return configureThirdPartyThread(object : ThreadTaskFactory {
        override fun createTask(thread: Thread): ThreadTask = threadTaskFactory(thread)
    }, overwrite)
}

fun JThread.configureThirdPartyThread(
    threadTaskFactory: ThreadTaskFactory = ExternalTaskFactory, overwrite: Boolean = false
): Thread {
    if (!overwrite && currentThreadLocal.get() != null) throw IllegalStateException("Already configured")
    val t = ThreadImpl(this, threadTaskFactory)
    currentThreadLocal.set(t)
    return t
}

private class ExternalTask(override val thread: Thread) : AbstractGameResource(DisabledResourceTracker), ThreadTask {
    override fun cleanup0(): CompletableFuture<Unit> = error("Can't cleanup")

    override fun run() = error("Can't run")
    override fun start(internalStart: () -> Unit) = internalStart()
}

private val currentThreadLocal: ThreadLocal<Thread> = ThreadLocal.withInitial {
    val thread = JThread.currentThread()
    if (thread is ThreadHolder) return@withInitial thread.thread
    null
}
private val rootThreadGroup = JThreadGroupCache[JThread.currentThread().threadGroup]

actual val Thread.Companion.currentThread: Thread
    get() {
        return currentThreadLocal.getOrSet {
            val thread = JThread.currentThread()
            if (thread is ForkJoinWorkerThread && thread.pool == ForkJoinPool.commonPool()) {
                // We want to support CompletableFuture async API so we want to support ForkJoinPool
                return@getOrSet ThreadImpl(thread, ExternalTaskFactory)
            }
            if (thread.isVirtual) {
                return@getOrSet ThreadImpl(thread, ExternalTaskFactory)
            }

            throw IllegalStateException("Current thread $thread is not a known thread")
        }
    }

actual val ThreadGroup.Companion.root: ThreadGroup
    get() = rootThreadGroup
