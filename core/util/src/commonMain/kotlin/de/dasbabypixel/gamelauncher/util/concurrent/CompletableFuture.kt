package de.dasbabypixel.gamelauncher.util.concurrent

import de.dasbabypixel.gamelauncher.util.function.GameBiConsumer
import de.dasbabypixel.gamelauncher.util.function.GameFunction

expect class CompletableFuture<T> {
    constructor()

    val isDone: Boolean

    fun whenComplete(map: (T?, Throwable?) -> Unit): CompletableFuture<T>

    fun whenComplete(map: GameBiConsumer<in T?, in Throwable?>): CompletableFuture<T>

    fun <V> thenApply(map: (T) -> V): CompletableFuture<V>

    fun <V> thenApply(map: GameFunction<in T, out V>): CompletableFuture<V>

    fun <V> thenApplyAsync(
        map: (T) -> V, executor: Executor
    ): CompletableFuture<V>

    fun <V> thenApplyAsync(
        map: GameFunction<in T, out V>, executor: Executor
    ): CompletableFuture<V>

    fun <V> thenApplyAsync(
        executor: Executor, map: (T) -> V
    ): CompletableFuture<V>

    fun <V> thenApplyAsync(
        executor: Executor, map: GameFunction<in T, out V>
    ): CompletableFuture<V>

    fun <V> thenCompose(map: (T) -> CompletableFuture<V>): CompletableFuture<V>

    fun <V> thenCompose(map: GameFunction<T, CompletableFuture<V>>): CompletableFuture<V>

    fun <V> thenComposeAsync(
        map: (T) -> CompletableFuture<V>, executor: Executor
    ): CompletableFuture<V>

    fun <V> thenComposeAsync(
        map: GameFunction<T, CompletableFuture<V>>, executor: Executor
    ): CompletableFuture<V>

    fun <V> thenComposeAsync(
        executor: Executor, map: (T) -> CompletableFuture<V>
    ): CompletableFuture<V>

    fun <V> thenComposeAsync(
        executor: Executor, map: GameFunction<T, CompletableFuture<V>>
    ): CompletableFuture<V>

    fun complete(value: T): Boolean

    fun completeExceptionally(t: Throwable): Boolean

    fun join(): T

    companion object {
        fun allOf(futures: Collection<CompletableFuture<*>>): CompletableFuture<Unit>
        fun allOf(vararg futures: CompletableFuture<*>): CompletableFuture<Unit>
    }
}

fun Collection<CompletableFuture<*>>.allComplete(): CompletableFuture<Unit> {
    return CompletableFuture.allOf(this)
}
