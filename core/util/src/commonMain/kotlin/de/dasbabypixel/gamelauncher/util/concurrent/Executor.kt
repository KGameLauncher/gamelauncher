package de.dasbabypixel.gamelauncher.util.concurrent

import de.dasbabypixel.gamelauncher.util.function.GameCallable
import de.dasbabypixel.gamelauncher.util.function.GameRunnable

@Suppress("RedundantModalityModifier")
expect interface Executor {
    open fun submit(runnable: GameRunnable): CompletableFuture<Unit>
    open fun submitGR(runnable: GameRunnable): CompletableFuture<Unit>
    fun <T> submit(callable: GameCallable<T>): CompletableFuture<T>
    open fun <T> submitGC(callable: GameCallable<T>): CompletableFuture<T>
}
