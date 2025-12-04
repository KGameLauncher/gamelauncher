package de.dasbabypixel.gamelauncher.impl.util.concurrent

import de.dasbabypixel.gamelauncher.api.util.concurrent.*
import de.dasbabypixel.gamelauncher.impl.provider.registerProvider
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinWorkerThread
import java.util.concurrent.locks.LockSupport
import java.lang.Thread as JThread
import java.lang.ThreadGroup as JThreadGroup

class CommonThreadGroup : ThreadGroup.Opened {
    override val name: String
    override val parent: ThreadGroup?
    val group: JThreadGroup

    constructor(group: JThreadGroup) {
        this.name = group.name
        this.parent = group.parent?.let { ThreadGroupCache[it] }
        this.group = group
    }

    constructor(name: String) : this(name, ThreadGroupCache[JThread.currentThread().threadGroup])

    constructor(name: String, parent: ThreadGroup) {
        this.name = name
        this.parent = parent
        this.group = JThreadGroup(ThreadGroupCache[parent], name)
    }
}

object ThreadGroupCache : InternalThreadGroupCache {
    init {
        registerProvider<InternalThreadGroupCache>("thread_group_cache")
    }

    private val map = WeakHashMap<JThreadGroup, ThreadGroup>()
    override operator fun get(group: JThreadGroup): ThreadGroup {
        synchronized(map) {
            return map.computeIfAbsent(
                group, ::CommonThreadGroup
            )
        }
    }

    override operator fun get(group: ThreadGroup): JThreadGroup {
        return (group as CommonThreadGroup).group
    }
}

object CommonThreadHelper : ThreadsProvider {
    val initialThread: JThread = JThread.currentThread()

    private class ForkJoinWrapper(thread: JThread) : AbstractThread(thread) {
        override fun run() = error("Can't run")

        override fun cleanup0(): CompletableFuture<Unit> = error("Can't cleanup")
    }

    private val currentThreadLocal: ThreadLocal<Thread> = ThreadLocal.withInitial {
        val thread = JThread.currentThread()
        if (thread === initialThread) return@withInitial mappedInitialThread
        if (thread is ThreadHolder) return@withInitial thread.thread
        if (thread is ForkJoinWorkerThread && thread.pool == ForkJoinPool.commonPool()) {
            // We want to support CompletableFuture async API so we want to support ForkJoinPool
            return@withInitial ForkJoinWrapper(thread)
        }

        throw IllegalStateException("Current thread $thread is not a ThreadHolder")
    }
    lateinit var mappedInitialThread: Thread
        private set

    init {
        @Suppress("UnusedExpression")
        ThreadGroupCache // init group cache
        registerProvider<ThreadsProvider>("threads_provider")
    }

    fun mapInitialThread(thread: Thread) {
        this.mappedInitialThread = thread
    }

    override fun currentThread(): Thread = currentThreadLocal.get()

    override fun park() {
        LockSupport.park()
    }

    override fun park(nanos: Long) {
        LockSupport.parkNanos(nanos)
    }

    override fun sleep(millis: Long) {
        JThread.sleep(millis)
    }

    override fun createGroup(name: String): ThreadGroup {
        return CommonThreadGroup(name)
    }

    override fun createGroup(name: String, parent: ThreadGroup): ThreadGroup {
        return CommonThreadGroup(name, parent)
    }
}
