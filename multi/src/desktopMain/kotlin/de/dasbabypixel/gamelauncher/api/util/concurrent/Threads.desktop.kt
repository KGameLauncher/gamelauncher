package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinWorkerThread
import kotlin.concurrent.getOrSet
import java.lang.Thread as JThread
import java.lang.ThreadGroup as JThreadGroup

private object ExternalTaskFactory : ThreadTaskFactory {
    override fun createTask(thread: Thread): ThreadTask = ExternalTask()
}

private class ExternalTask : AbstractGameResource(disabledThreadTracker), ThreadTask {
    override fun cleanup0(): CompletableFuture<Unit> = error("Can't cleanup")

    override fun run() = error("Can't run")
    override fun start(internalStart: () -> Unit) = internalStart()
}

private val disabledThreadTracker = ResourceTracker(false)
private val currentThreadLocal: ThreadLocal<Thread> = ThreadLocal.withInitial {
    val thread = JThread.currentThread()
    if (thread is ThreadHolder) return@withInitial thread.thread
    null
}
private val rootThreadGroup = JThreadGroupCache[JThread.currentThread().threadGroup]

fun JThread.configureThirdPartyThread(
    threadTaskFactory: ThreadTaskFactory = ExternalTaskFactory, overwrite: Boolean = false
): Thread {
    if (!overwrite && currentThreadLocal.get() != null) throw IllegalStateException("Already configured")
    val t = ThreadImpl(this, threadTaskFactory)
    currentThreadLocal.set(t)
    return t
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

actual fun Thread.Companion.create(
    name: String, taskFactory: ThreadTaskFactory, daemon: Boolean, group: ThreadGroup
): Thread {
    return JThreadImpl(name, JThreadGroupCache[group], taskFactory, daemon).thread
}

actual val ThreadGroup.Companion.root: ThreadGroup
    get() = rootThreadGroup
