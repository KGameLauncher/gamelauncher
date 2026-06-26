package de.dasbabypixel.gamelauncher.api.resource

import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.api.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.api.util.concurrent.currentThread
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.util.GameException
import de.dasbabypixel.gamelauncher.util.stack.StackTrace
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
abstract class AbstractGameResource : GameResource.StackCapable {
    companion object {
        private val logger by getLogger()
    }

    final override var creationStack: StackTrace? = null
        private set
    final override var creationThreadName: String? = null
        private set
    final override var cleanupStack: StackTrace? = null
        private set
    final override var cleanupThreadName: String? = null
        private set

    protected val tracker: ResourceTracker
    private val created = AtomicBoolean(false)
    private val calledCleanup = AtomicBoolean(false)

    final override val cleanedUp: Boolean
        get() = cleanupFuture.isDone
    final override val cleanupFuture: CompletableFuture<Unit> = CompletableFuture()

    open val autoTrack
        get() = true

    constructor(tracker: ResourceTracker) {
        this.tracker = tracker

        if (this.autoTrack) {
            track(dropStack = 2u)
        }
    }

    fun stopTracking() = stopTracking(tracker)

    protected fun track(thread: Thread? = null, dropStack: UInt = 0u) {
        if (!created.compareAndSet(expectedValue = false,
                newValue = true)
        ) throw IllegalStateException("Already tracked")
        if (tracker.enabled) {
            val thread = thread ?: currentThread
            creationThreadName = thread.name
            creationStack = thread.stackTrace.drop(dropStack)
            startTracking(tracker)

            cleanupFuture.whenComplete { _, _ ->
                stopTracking(tracker)
            }
        }
    }

    protected abstract fun cleanup0(): CompletableFuture<Unit>?

    final override fun cleanupAsync(): CompletableFuture<Unit> {
        if (!created.load()) throw IllegalStateException("Resource was never tracked")
        if (calledCleanup.compareAndSet(expectedValue = false, newValue = true)) {
            if (tracker.enabled) {
                val thread = currentThread
                cleanupStack = thread.stackTrace
                cleanupThreadName = thread.name
            }
            val f = try {
                cleanup0()
            } catch (ex: Throwable) {
                stopTracking(tracker)
                cleanupFuture.completeExceptionally(ex)
                return cleanupFuture
            }
            if (f == null) {
                stopTracking(tracker)
                cleanupFuture.complete(Unit)
            } else {
                f.whenComplete { _, t ->
                    stopTracking(tracker)
                    if (t != null) cleanupFuture.completeExceptionally(t)
                    else cleanupFuture.complete(Unit)
                }
            }
        } else {
            val ex = GameException("Multiple cleanups")
            if (tracker.enabled) {
                val creation = GameException("CreationStack: $creationThreadName")
                creation.stackTrace = creationStack!!
                val cleanup = GameException("CleanupStack: $cleanupThreadName")
                cleanup.stackTrace = cleanupStack!!
                ex.addSuppressed(creation)
                ex.addSuppressed(cleanup)
            }
            logger.error("Failed to cleanup GameResource", ex)
            stopTracking(tracker) // Stop tracking, we already printed to console
        }
        return cleanupFuture
    }
}