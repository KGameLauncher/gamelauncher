package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.GameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.resource.stopTracking
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.function.GameCallable
import de.dasbabypixel.gamelauncher.api.util.function.GameRunnable
import de.dasbabypixel.gamelauncher.api.util.function.toCallable
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.stack.StackTrace
import de.dasbabypixel.gamelauncher.api.util.stack.StackTraceSnapshot
import de.dasbabypixel.gamelauncher.impl.Providable
import kotlin.concurrent.Volatile

expect open class NativeThread {
    constructor(threadGroup: NativeThreadGroup, runnable: GameRunnable)
    constructor(threadGroup: NativeThreadGroup, runnable: GameRunnable, name: String)

    expect companion object {
        fun unpark(thread: NativeThread)
    }
}

expect val NativeThread.stackTrace: StackTrace
expect var NativeThread.name: String
expect var NativeThread.isDaemon: Boolean
expect var NativeThread.threadGroup: NativeThread
expect fun NativeThread.start()

expect class NativeThreadGroup

internal expect val provider: ThreadsProvider
internal expect val ThreadGroupCache: InternalThreadGroupCache

@Suppress("LeakingThis")
abstract class AbstractThread : AbstractGameResource, Thread {
    companion object {
        val logger = getLogger<AbstractThread>()
    }

    constructor(tracker: ResourceTracker, group: ThreadGroup) : super(tracker) {
        this.nativeThread = ThreadImpl(this, group, ::run0)
        this.group = ThreadGroupCache[nativeThread.threadGroup]
    }

    constructor(tracker: ResourceTracker, group: ThreadGroup, daemon: Boolean) : this(tracker, group) {
        nativeThread.isDaemon = daemon
    }

    constructor(tracker: ResourceTracker, group: ThreadGroup, name: String) : super(tracker) {
        this.nativeThread = ThreadImpl(this, group, ::run0, name)
        this.group = ThreadGroupCache[nativeThread.threadGroup]
    }

    constructor(tracker: ResourceTracker, group: ThreadGroup, name: String, daemon: Boolean) : this(tracker,
        group,
        name) {
        nativeThread.isDaemon = daemon
    }

    protected constructor(tracker: ResourceTracker, thread: NativeThread) : super(tracker) {
        this.nativeThread = thread
        this.group = ThreadGroupCache[thread.threadGroup]
    }

    protected val nativeThread: NativeThread
    final override val group: ThreadGroup

    final override val stacktrace: StackTrace
        get() = nativeThread.stackTrace
    final override val name: String
        get() = nativeThread.name
    override val autoTrack: Boolean
        get() = customStart
    protected open val customStart: Boolean
        get() = false

    final override fun start() {
        if (customStart) {
            customStart()
        } else {
            track()
            nativeThread.start()
            logger.info("Started thread $name[${group.name}]")
        }
    }

    protected open fun customStart() {
    }

    final override fun unpark() {
        NativeThread.unpark(nativeThread)
    }

    protected abstract fun run()

    protected fun run0() {
        try {
            run()
        } catch (e: Throwable) {
            logger.error("Uncaught exception in $name", e)
            stopTracking(tracker)
        }
    }

    private class ThreadImpl : NativeThread, ThreadHolder {
        override val thread: Thread

        constructor(thread: Thread, group: ThreadGroup, runnable: GameRunnable) : super(ThreadGroupCache[group],
            runnable) {
            this.thread = thread
        }

        constructor(
            thread: Thread, group: ThreadGroup, runnable: GameRunnable, name: String
        ) : super(ThreadGroupCache[group], runnable, name) {
            this.thread = thread
        }
    }
}


interface InternalThreadGroupCache : Providable {
    operator fun get(group: ThreadGroup): NativeThreadGroup
    operator fun get(group: NativeThreadGroup): ThreadGroup
}

abstract class AbstractExecutorThread : AbstractThread, ExecutorThread, StackTraceSnapshot.CauseContainer {
    companion object {
        private val logger = getLogger<AbstractExecutorThread>()
    }

    final override var cause: StackTraceSnapshot? = null
    private val mpsc: MPSC<QueueEntry<in Any>> = EfficientMPSCs.create(::QueueEntry, 1024)
    private val poller = mpsc.createPoller { e, _ ->
        if (StackTraceSnapshot.calculateThreadStacks) cause = e.entry
        try {
            e.execute()
        } catch (t: Throwable) {
            val ex = buildStackTrace(t)
            ex.stacktrace = StackTrace(emptyArray())
            logger.error("Failed to execute task {}", e.call, ex)
        }
        e.clear()
        if (StackTraceSnapshot.calculateThreadStacks) cause = null
        true
    }
    private val publisher =
        mpsc.createPublisher<CompletableFuture<*>, StackTraceSnapshot, GameCallable<*>> { e, a, b, c -> e.set(b, c, a) }
    private val exitFuture = CompletableFuture<Unit>()

    @Volatile
    private var exit = false
    private var exitComplete = false
    private val customAwaitingSystem: Boolean
    protected val workSignal = Signal()

    constructor(
        tracker: ResourceTracker, group: ThreadGroup, customAwaitingSystem: Boolean = false
    ) : super(tracker, group) {
        this.customAwaitingSystem = customAwaitingSystem
    }

    constructor(
        tracker: ResourceTracker, group: ThreadGroup, daemon: Boolean, customAwaitingSystem: Boolean = false
    ) : super(tracker, group, daemon) {
        this.customAwaitingSystem = customAwaitingSystem
    }

    constructor(
        tracker: ResourceTracker, group: ThreadGroup, name: String, customAwaitingSystem: Boolean = false
    ) : super(tracker, group, name) {
        this.customAwaitingSystem = customAwaitingSystem
    }

    constructor(
        tracker: ResourceTracker,
        group: ThreadGroup,
        name: String,
        daemon: Boolean,
        customAwaitingSystem: Boolean = false
    ) : super(tracker, group, name, daemon) {
        this.customAwaitingSystem = customAwaitingSystem
    }

    protected constructor(
        tracker: ResourceTracker, thread: NativeThread, customAwaitingSystem: Boolean
    ) : super(tracker, thread) {
        this.customAwaitingSystem = customAwaitingSystem
    }

    final override fun run() {
        logger.debug("Starting $name")
        try {
            startExecuting()
            while (!shouldExit()) {
                loop()
            }
            if (exitComplete) return
            completeExit()
        } catch (t: Throwable) {
            logger.error("Exception in thread {}", thread.name, t)
            exitFuture.completeExceptionally(t)
        } finally {
            logger.debug("Stopping $name")
            exitFuture.complete(Unit)
        }
    }

    private fun completeExit() {
        exitComplete = true
        workQueue()
        workExecution()
        stopExecuting()
        exitFuture.complete(Unit)
    }

    private fun loop() {
        preLoop()
        workQueue()
        workExecution()
        postLoop()
    }

    protected open fun preLoop() {
        waitForSignal()
    }

    protected open fun postLoop() {
    }

    protected open fun customSignal() = Unit
    protected open fun customAwaitWork() = Unit

    protected fun signal() {
        if (customAwaitingSystem) {
            customSignal()
        } else {
            workSignal.signal()
        }
    }

    protected fun waitForSignal() {
        if (customAwaitingSystem) {
            customAwaitWork()
        } else {
            workSignal.await()
        }
    }

    override fun <T> submit(callable: GameCallable<T>): CompletableFuture<T> {
        if (exit) throw GameException("No new tasks can be submitted. The executor has been shut down")
        val fut = CompletableFuture<T>()
        if (currentThread == this) {
            work(callable, fut)
        } else {
            publisher.publish(fut, StackTraceSnapshot.new(), callable)
            if (exit) throw GameException("No new tasks can be submitted. The executor has been shut down")
            signal()
        }
        return fut
    }

    private fun exit(): CompletableFuture<Unit> {
        if (currentThread == this) {
            throw Error("Thread may not be cleaned up by itself. Spawn another thread!")
        }
        exit = true
        signal()
        return exitFuture
    }

    final override fun cleanup0(): CompletableFuture<Unit> = exit()

    protected open fun shouldExit(): Boolean {
        return exit
    }

    private fun <T> work(call: GameCallable<T>, future: CompletableFuture<T>) {
        try {
            future.complete(call.call())
        } catch (ex: Throwable) {
            val ex2 = buildStackTrace(ex)
            logger.error("Failed to execute task $call", ex2)
            future.completeExceptionally(ex)
        }
    }

    fun buildStackTrace(cause: Throwable?): GameException {
        val ex = GameException("Exception in ExecutorThread", cause)
        ex.stacktrace = StackTrace(emptyArray())
        val c = this.cause
        if (c != null) {
            val t = c.buildCause()
            ex.addSuppressed(t)
        }
        return ex
    }

    private fun workQueue() {
        poller.poll()
    }

    protected open fun startExecuting() {}
    protected open fun workExecution() {}
    protected open fun stopExecuting() {}

    private inner class QueueEntry<T : Any>(
        var entry: StackTraceSnapshot? = null,
        var call: GameCallable<T>? = null,
        var future: CompletableFuture<T>? = null
    ) {
        @Suppress("UNCHECKED_CAST")
        fun set(entry: StackTraceSnapshot?, call: GameCallable<*>?, future: CompletableFuture<*>?) {
            this.entry = entry
            this.call = call as GameCallable<T>?
            this.future = future as CompletableFuture<T>?
        }

        fun execute() {
            work(call!!, future!!)
        }

        fun clear() {
            entry = null
            call = null
            future = null
        }
    }
}

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
    val stacktrace: StackTrace
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
    fun submit(runnable: GameRunnable): CompletableFuture<Unit> = submit(runnable.toCallable())
    fun submitGR(runnable: GameRunnable) = submit(runnable)
    fun <T> submit(callable: GameCallable<T>): CompletableFuture<T>
    fun <T> submitGC(callable: GameCallable<T>): CompletableFuture<T> = submit(callable)
}
