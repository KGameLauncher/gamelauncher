package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.util.concurrent.ExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.api.util.function.GameCallable
import java.util.concurrent.CompletableFuture

interface WrappingExecutorThread : ExecutorThread {
    val handle: ExecutorThread
    override val name: String
        get() = handle.name
    override val group: ThreadGroup
        get() = handle.group
    override val stackTrace: Array<StackTraceElement>
        get() = handle.stackTrace

    override fun start() {
        handle.start()
    }

    override fun unpark() {
        handle.unpark()
    }

    override val cleanedUp: Boolean
        get() = handle.cleanedUp
    override val cleanupFuture: CompletableFuture<Unit>
        get() = handle.cleanupFuture

    override val thread: Thread
        get() = handle

    override fun cleanup(): CompletableFuture<Unit> = handle.cleanup()

    override fun <T> submit(callable: GameCallable<T>): CompletableFuture<T> = handle.submit(callable)
}