package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.function.GameCallable
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.util.resource.ResourceTracker.stopTracking
import de.dasbabypixel.gamelauncher.impl.provider.Providable
import de.dasbabypixel.gamelauncher.impl.provider.provide
import java.util.concurrent.CompletableFuture
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.locks.ReentrantLock
import java.lang.Thread as JThread
import java.lang.ThreadGroup as JThreadGroup

@Suppress("LeakingThis")
abstract class AbstractThread : AbstractGameResource, Thread {
    companion object {
        val logger = getLogger<AbstractThread>()
    }

    constructor(group: ThreadGroup) {
        this.threadImpl = ThreadImpl(this, group, ::run0)
        this.group = ThreadGroupCache[threadImpl.threadGroup]
    }

    constructor(group: ThreadGroup, daemon: Boolean) : this(group) {
        threadImpl.isDaemon = daemon
    }

    constructor(group: ThreadGroup, name: String) {
        this.threadImpl = ThreadImpl(this, group, ::run0, name)
        this.group = ThreadGroupCache[threadImpl.threadGroup]
    }

    constructor(group: ThreadGroup, name: String, daemon: Boolean) : this(group, name) {
        threadImpl.isDaemon = daemon
    }

    protected constructor(thread: JThread) {
        this.threadImpl = thread
        this.group = ThreadGroupCache[thread.threadGroup]
    }

    protected val threadImpl: JThread
    final override val group: ThreadGroup

    final override val stackTrace: Array<StackTraceElement>
        get() = threadImpl.stackTrace
    final override val name: String
        get() = threadImpl.name
    override val autoTrack: Boolean
        get() = customStart
    protected open val customStart: Boolean
        get() = false

    final override fun start() {
        if (customStart) {
            customStart()
        } else {
            track()
            threadImpl.start()
            logger.info("Started thread $name[${group.name}]")
        }
    }

    protected open fun customStart() {
    }

    final override fun unpark() {
        LockSupport.unpark(threadImpl)
    }
    protected abstract fun run()

    protected fun run0() {
        try {
            run()
        } catch (e: Throwable) {
            logger.error("Uncaught exception in $name", e)
            stopTracking()
        }
    }

    private class ThreadImpl : JThread, ThreadHolder {
        override val thread: Thread

        constructor(thread: Thread, group: ThreadGroup, runnable: Runnable) : super(
            ThreadGroupCache[group], runnable
        ) {
            this.thread = thread
        }

        constructor(thread: Thread, group: ThreadGroup, runnable: Runnable, name: String) : super(
            ThreadGroupCache[group], runnable, name
        ) {
            this.thread = thread
        }
    }
}

private val ThreadGroupCache = provide<InternalThreadGroupCache>()

interface InternalThreadGroupCache : Providable {
    operator fun get(group: ThreadGroup): JThreadGroup
    operator fun get(group: JThreadGroup): ThreadGroup
}

abstract class AbstractExecutorThread : AbstractThread, ExecutorThread, StackTraceSnapshot.CauseContainer {
    companion object {
        private val logger = getLogger<AbstractExecutorThread>()
    }

    final override var cause: StackTraceSnapshot? = null
    private val mpsc: MPSC<QueueEntry<in Any>> = EfficientMPSCs.create(::QueueEntry, 1024)
    private val poller = mpsc.createPoller { e, _ ->
        if (Debug.calculateThreadStacks) cause = e.entry
        try {
            e.execute()
        } catch (t: Throwable) {
            val ex = buildStackTrace()
            ex.stackTrace = t.stackTrace
            ex.initCause(t)
            logger.error("Failed to execute task {}", e.call, ex)
        }
        e.clear()
        if (Debug.calculateThreadStacks) cause = null
        true
    }
    private val publisher =
        mpsc.createPublisher<CompletableFuture<*>, StackTraceSnapshot, GameCallable<*>> { e, a, b, c -> e.set(b, c, a) }
    private val exitFuture = CompletableFuture<Unit>()

    @Volatile
    private var exit = false
    private var exitComplete = false
    protected val lock = ReentrantLock()
    protected val count = AtomicInteger(0)
    protected val hasWork = lock.newCondition()
    protected val hasWorkBool = AtomicBoolean(false)

    constructor(group: ThreadGroup) : super(group)
    constructor(group: ThreadGroup, daemon: Boolean) : super(group, daemon)
    constructor(group: ThreadGroup, name: String) : super(group, name)
    constructor(group: ThreadGroup, name: String, daemon: Boolean) : super(group, name, daemon)
    protected constructor(thread: JThread) : super(thread)

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
        if (shouldWaitForSignal()) {
            waitForSignal()
        }
    }

    protected open fun postLoop() {
    }

    protected open fun signal() {
        lock.lock()
        try {
            if (customSignal()) return
            hasWorkBool.set(true)
            hasWork.signal()
        } finally {
            lock.unlock()
        }
    }

    protected open fun customSignal(): Boolean = false

    protected open fun waitForSignal() {
        if (hasWorkBool.compareAndSet(true, false)) return

        val custom1 = customAwait()
        val custom: Boolean
        if (!custom1) {
            lock.lock()
            try {
                custom = awaitWork()
            } finally {
                lock.unlock()
            }
            if (custom) {
                customAwaitWork()
            }
        } else {
            customAwaitWork()
        }
    }

    protected open fun customAwait(): Boolean {
        return false
    }

    protected open fun awaitWork(): Boolean {
        if (!hasWorkBool.compareAndSet(true, false)) {
            hasWork.await()
        }
        return false
    }

    protected open fun customAwaitWork() {
    }

    protected open fun shouldWaitForSignal(): Boolean {
        return true
    }

    override fun <T> submit(callable: GameCallable<T>): CompletableFuture<T> {
        if (exit) throw RejectedExecutionException("No new tasks can be submitted. The executor has been shut down")
        val fut = CompletableFuture<T>()
        if (currentThread == this) {
            work(callable, fut)
        } else {
            publisher.publish(fut, StackTraceSnapshot.new(), callable)
            if (exit) throw RejectedExecutionException("No new tasks can be submitted. The executor has been shut down")
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
            val ex2 = buildStackTrace()
            ex2.initCause(ex)
            logger.error("Failed to execute task $call", ex2)
            future.completeExceptionally(ex)
        }
    }

    fun buildStackTrace(): GameException {
        val ex = GameException("Exception in ExecutorThread")
        ex.stackTrace = emptyArray()
        val c = cause
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

