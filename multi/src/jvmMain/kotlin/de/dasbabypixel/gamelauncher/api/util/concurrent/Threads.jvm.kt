package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.buildStackTrace
import de.dasbabypixel.gamelauncher.api.util.stack.StackTrace
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinWorkerThread
import java.util.concurrent.locks.LockSupport
import java.lang.Thread as JThread

//
//    actual constructor(threadGroup: NativeThreadGroup, runnable: GameRunnable) : super(
//        threadGroup, runnable.toRunnable()
//    )
//
//    actual constructor(threadGroup: NativeThreadGroup, runnable: GameRunnable, name: String) : super(
//        threadGroup, runnable.toRunnable(), name
//    )
//
//    actual companion object {
//        private fun GameRunnable.toRunnable(): Runnable {
//            return { this.run() }
//        }
//
//        actual fun unpark(thread: NativeThread) {
//            LockSupport.unpark(thread)
//        }
//    }


actual val NativeThread.stackTrace: StackTrace
    get() = buildStackTrace(stackTrace)
actual var NativeThread.name: String
    set(value) {
        name = value
    }
    get() = name
actual var NativeThread.isDaemon: Boolean
    set(value) {
        isDaemon = value
    }
    get() = isDaemon
actual val NativeThread.threadGroup: NativeThreadGroup
    get() = threadGroup

actual fun NativeThread.start() = start()

internal actual fun NativeThreads.unpark(thread: NativeThread) = LockSupport.unpark(thread)
internal actual val NativeThreads.currentThread: NativeThread
    get() = NativeThread.currentThread()

actual typealias NativeThread = JThread
actual typealias NativeThreadTask = Runnable
actual typealias NativeThreadGroup = java.lang.ThreadGroup

internal actual val provider: ThreadsProvider = CommonThreadHelper

class CommonThreadGroup : ThreadGroup.Opened {
    override val name: String
    override val parent: ThreadGroup?
    val group: NativeThreadGroup

    constructor(group: NativeThreadGroup) {
        this.name = group.name
        this.parent = group.parent?.let { ThreadGroupCache[it] }
        this.group = group
    }

    constructor(name: String) : this(name, ThreadGroupCache[JThread.currentThread().threadGroup])

    constructor(name: String, parent: ThreadGroup) {
        this.name = name
        this.parent = parent
        this.group = NativeThreadGroup(ThreadGroupCache[parent], name)
    }
}

internal actual val ThreadGroupCache: InternalThreadGroupCache = JavaThreadGroupCache

private object JavaThreadGroupCache : InternalThreadGroupCache {
    private val map = WeakHashMap<NativeThreadGroup, ThreadGroup>()
    override operator fun get(group: NativeThreadGroup): ThreadGroup {
        synchronized(map) {
            return map.computeIfAbsent(group, ::CommonThreadGroup)
        }
    }

    override operator fun get(group: ThreadGroup): NativeThreadGroup {
        return (group as CommonThreadGroup).group
    }
}

object CommonThreadHelper : ThreadsProvider {
    val initialThread: JThread = JThread.currentThread()
    private val disabledThreadTracker = ResourceTracker(false)

    private class ForkJoinWrapper(tracker: ResourceTracker, thread: NativeThread) : AbstractThread(tracker, thread) {
        override fun run() = error("Can't run")

        override fun cleanup0(): CompletableFuture<Unit> = error("Can't cleanup")
    }

    private class VirtualThreadWrapper(tracker: ResourceTracker, thread: NativeThread) :
        AbstractThread(tracker, thread) {
        override fun run() = error("Can't run")

        override fun cleanup0(): CompletableFuture<Unit> = error("Can't cleanup")
    }

    private val currentThreadLocal: ThreadLocal<Thread> = ThreadLocal.withInitial {
        val thread = JThread.currentThread()
        if (thread === initialThread) return@withInitial mappedInitialThread
        if (thread is ThreadHolder) return@withInitial thread.thread
        if (thread is ForkJoinWorkerThread && thread.pool == ForkJoinPool.commonPool()) {
            // We want to support CompletableFuture async API so we want to support ForkJoinPool
            return@withInitial ForkJoinWrapper(disabledThreadTracker, thread)
        }
        if (thread.isVirtual) {
            return@withInitial VirtualThreadWrapper(disabledThreadTracker, thread)
        }

        throw IllegalStateException("Current thread $thread is not a ThreadHolder")
    }
    lateinit var mappedInitialThread: Thread
        private set

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
