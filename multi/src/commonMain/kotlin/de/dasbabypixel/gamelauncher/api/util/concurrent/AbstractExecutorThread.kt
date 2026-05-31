package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.function.GameCallable
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.stack.StackTrace
import de.dasbabypixel.gamelauncher.api.util.stack.StackTraceSnapshot
import kotlin.concurrent.Volatile

abstract class AbstractExecutorThread(
    tracker: ResourceTracker, thread: Thread, private val customAwaitingSystem: Boolean = false
) : AbstractThreadTask(tracker, thread), StackTraceSnapshot.CauseContainer, ExecutorThreadTask {
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
        mpsc.createPublisher<CompletableFuture<*>, StackTraceSnapshot, GameCallable<*>> { e, a, b, c ->
            e.set(
                b, c, a
            )
        }
    private val exitFuture = CompletableFuture<Unit>()

    @Volatile
    private var exit = false
    private var exitComplete = false
    protected val workSignal = Signal()

    final override fun run() {
        logger.debug("Starting ${thread.name}")
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
            logger.debug("Stopping ${thread.name}")
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
