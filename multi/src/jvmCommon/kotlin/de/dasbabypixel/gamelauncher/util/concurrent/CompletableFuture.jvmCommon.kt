@file:Suppress("NOTHING_TO_INLINE")

package de.dasbabypixel.gamelauncher.util.concurrent

import de.dasbabypixel.gamelauncher.api.util.function.GameBiConsumer
import de.dasbabypixel.gamelauncher.api.util.function.GameFunction
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import java.util.concurrent.CompletableFuture as JCF
import java.util.concurrent.Executor as JExecutor

@Suppress("NewApi")
actual class CompletableFuture<T> : JCF<T> {
    actual constructor()

    @get:JvmName("customIsDone")
    actual val isDone: Boolean
        get() = super.isDone

    //region overrides
    override fun <U> newIncompleteFuture(): CompletableFuture<U> = CompletableFuture()

    override fun whenComplete(action: BiConsumer<in T, in Throwable>): CompletableFuture<T?> =
        super.whenComplete(action).cast

    override fun <U> thenApply(fn: Function<in T, out U?>): CompletableFuture<U?> = super.thenApply(fn).cast

    override fun <U> thenApplyAsync(fn: Function<in T, out U?>): CompletableFuture<U?> = super.thenApplyAsync(fn).cast

    override fun <U> thenApplyAsync(
        fn: Function<in T, out U?>, executor: JExecutor?
    ): CompletableFuture<U?> = super.thenApplyAsync(fn, executor).cast

    override fun <U> thenCompose(fn: Function<in T, out CompletionStage<U?>?>): CompletableFuture<U?> =
        super.thenCompose(fn).cast

    override fun <U> thenComposeAsync(fn: Function<in T, out CompletionStage<U?>?>): CompletableFuture<U?> =
        super.thenComposeAsync(fn).cast

    override fun <U> thenComposeAsync(
        fn: Function<in T, out CompletionStage<U?>?>, executor: JExecutor?
    ): CompletableFuture<U?> = super.thenComposeAsync(fn, executor).cast

    override fun thenAccept(action: Consumer<in T>): CompletableFuture<Void?> = super.thenAccept(action).cast

    override fun thenAcceptAsync(action: Consumer<in T>): CompletableFuture<Void?> = super.thenAcceptAsync(action).cast

    override fun thenAcceptAsync(
        action: Consumer<in T>, executor: JExecutor?
    ): CompletableFuture<Void?> = super.thenAcceptAsync(action, executor).cast

    override fun thenRun(action: Runnable): CompletableFuture<Void?> = super.thenRun(action).cast

    override fun thenRunAsync(action: Runnable): CompletableFuture<Void?> = super.thenRunAsync(action).cast

    override fun thenRunAsync(
        action: Runnable, executor: JExecutor?
    ): CompletableFuture<Void?> = super.thenRunAsync(action, executor).cast

    override fun <U, V> thenCombine(
        other: CompletionStage<out U?>, fn: BiFunction<in T, in U, out V?>
    ): CompletableFuture<V?> = super.thenCombine(other, fn).cast

    override fun <U, V> thenCombineAsync(
        other: CompletionStage<out U?>, fn: BiFunction<in T, in U, out V?>
    ): CompletableFuture<V?> = super.thenCombineAsync(other, fn).cast

    override fun <U, V> thenCombineAsync(
        other: CompletionStage<out U?>, fn: BiFunction<in T, in U, out V?>, executor: JExecutor?
    ): CompletableFuture<V?> = super.thenCombineAsync(other, fn, executor).cast

    override fun <U> thenAcceptBoth(
        other: CompletionStage<out U?>, action: BiConsumer<in T, in U>
    ): CompletableFuture<Void?> = super.thenAcceptBoth(other, action).cast

    override fun <U> thenAcceptBothAsync(
        other: CompletionStage<out U?>, action: BiConsumer<in T, in U>
    ): CompletableFuture<Void?> = super.thenAcceptBothAsync(other, action).cast

    override fun <U> thenAcceptBothAsync(
        other: CompletionStage<out U?>, action: BiConsumer<in T, in U>, executor: JExecutor?
    ): CompletableFuture<Void?> = super.thenAcceptBothAsync(other, action, executor).cast

    override fun runAfterBoth(
        other: CompletionStage<*>, action: Runnable
    ): CompletableFuture<Void?> = super.runAfterBoth(other, action).cast

    override fun runAfterBothAsync(
        other: CompletionStage<*>, action: Runnable
    ): CompletableFuture<Void?> = super.runAfterBothAsync(other, action).cast

    override fun runAfterBothAsync(
        other: CompletionStage<*>, action: Runnable, executor: JExecutor?
    ): CompletableFuture<Void?> = super.runAfterBothAsync(other, action, executor).cast

    override fun <U> applyToEither(
        other: CompletionStage<out T?>, fn: Function<in T, U?>
    ): CompletableFuture<U?> = super.applyToEither(other, fn).cast

    override fun <U> applyToEitherAsync(
        other: CompletionStage<out T?>, fn: Function<in T, U?>
    ): CompletableFuture<U?> = super.applyToEitherAsync(other, fn).cast

    override fun <U> applyToEitherAsync(
        other: CompletionStage<out T?>, fn: Function<in T, U?>, executor: JExecutor?
    ): CompletableFuture<U?> = super.applyToEitherAsync(other, fn, executor).cast

    override fun acceptEither(
        other: CompletionStage<out T?>, action: Consumer<in T>
    ): CompletableFuture<Void?> = super.acceptEither(other, action).cast

    override fun acceptEitherAsync(
        other: CompletionStage<out T?>, action: Consumer<in T>
    ): CompletableFuture<Void?> = super.acceptEitherAsync(other, action).cast

    override fun acceptEitherAsync(
        other: CompletionStage<out T?>, action: Consumer<in T>, executor: JExecutor?
    ): CompletableFuture<Void?> = super.acceptEitherAsync(other, action, executor).cast

    override fun runAfterEither(
        other: CompletionStage<*>, action: Runnable
    ): CompletableFuture<Void?> = super.runAfterEither(other, action).cast

    override fun runAfterEitherAsync(
        other: CompletionStage<*>, action: Runnable
    ): CompletableFuture<Void?> = super.runAfterEitherAsync(other, action).cast

    override fun runAfterEitherAsync(
        other: CompletionStage<*>, action: Runnable, executor: JExecutor?
    ): CompletableFuture<Void?> = super.runAfterEitherAsync(other, action, executor).cast

    override fun whenCompleteAsync(action: BiConsumer<in T, in Throwable>): CompletableFuture<T?> =
        super.whenCompleteAsync(action).cast

    override fun whenCompleteAsync(
        action: BiConsumer<in T, in Throwable>, executor: JExecutor?
    ): CompletableFuture<T?> = super.whenCompleteAsync(action, executor).cast

    override fun <U> handle(fn: BiFunction<in T, Throwable?, out U?>): CompletableFuture<U?> = super.handle(fn).cast

    override fun <U> handleAsync(fn: BiFunction<in T, Throwable?, out U?>): CompletableFuture<U?> =
        super.handleAsync(fn).cast

    override fun <U> handleAsync(
        fn: BiFunction<in T, Throwable?, out U?>, executor: JExecutor?
    ): CompletableFuture<U?> = super.handleAsync(fn, executor).cast

    override fun toCompletableFuture(): CompletableFuture<T?> = super.toCompletableFuture().cast

    override fun exceptionally(fn: Function<Throwable?, out T?>): CompletableFuture<T?> = super.exceptionally(fn).cast

    override fun exceptionallyAsync(fn: Function<Throwable?, out T?>): CompletableFuture<T?> =
        super.exceptionallyAsync(fn).cast

    override fun exceptionallyAsync(
        fn: Function<Throwable?, out T?>, executor: JExecutor?
    ): CompletableFuture<T?> = super.exceptionallyAsync(fn, executor).cast

    override fun exceptionallyCompose(fn: Function<Throwable?, out CompletionStage<T?>?>): CompletableFuture<T?> =
        super.exceptionallyCompose(fn).cast

    override fun exceptionallyComposeAsync(fn: Function<Throwable?, out CompletionStage<T?>?>): CompletableFuture<T?> =
        super.exceptionallyComposeAsync(fn).cast

    override fun exceptionallyComposeAsync(
        fn: Function<Throwable?, out CompletionStage<T?>?>, executor: JExecutor?
    ): CompletableFuture<T?> = super.exceptionallyComposeAsync(fn, executor).cast

    override fun completeAsync(
        supplier: Supplier<out T?>, executor: JExecutor?
    ): CompletableFuture<T?> = super.completeAsync(supplier, executor).cast

    override fun completeAsync(supplier: Supplier<out T?>): CompletableFuture<T?> = super.completeAsync(supplier).cast

    override fun completeOnTimeout(
        value: T?, timeout: Long, unit: TimeUnit
    ): CompletableFuture<T?> = super.completeOnTimeout(value, timeout, unit).cast

    override fun orTimeout(
        timeout: Long, unit: TimeUnit
    ): CompletableFuture<T?> = super.orTimeout(timeout, unit).cast

    override fun minimalCompletionStage(): CompletionStage<T?> = TODO("Minimal is not supported at this point")

    override fun copy(): CompletableFuture<T?> = super.copy().cast

    //endregion

    //region Actual Functions
    actual inline fun whenComplete(noinline map: (T?, Throwable?) -> Unit): CompletableFuture<T> =
        ths.whenComplete(map).cast

    actual inline fun whenComplete(map: GameBiConsumer<in T?, in Throwable?>): CompletableFuture<T> =
        ths.whenComplete(map).cast

    actual inline fun <V> thenApply(noinline map: (T) -> V): CompletableFuture<V> = ths.thenApply(map).cast

    actual inline fun <V> thenApply(map: GameFunction<in T, out V>): CompletableFuture<V> = ths.thenApply(map).cast

    actual inline fun <V> thenApplyAsync(
        noinline map: (T) -> V, executor: Executor
    ): CompletableFuture<V> = ths.thenApplyAsync(map, executor).cast

    actual inline fun <V> thenApplyAsync(
        map: GameFunction<in T, out V>, executor: Executor
    ): CompletableFuture<V> = ths.thenApplyAsync(map, executor).cast

    actual inline fun <V> thenApplyAsync(
        executor: Executor, noinline map: (T) -> V
    ): CompletableFuture<V> = ths.thenApplyAsync(map, executor).cast

    actual inline fun <V> thenApplyAsync(
        executor: Executor, map: GameFunction<in T, out V>
    ): CompletableFuture<V> = ths.thenApplyAsync(map, executor).cast

    actual inline fun <V> thenCompose(noinline map: (T) -> CompletableFuture<V>): CompletableFuture<V> =
        ths.thenCompose(map).cast

    actual inline fun <V> thenCompose(map: GameFunction<T, CompletableFuture<V>>): CompletableFuture<V> =
        ths.thenCompose(map).cast

    actual inline fun <V> thenComposeAsync(
        noinline map: (T) -> CompletableFuture<V>, executor: Executor
    ): CompletableFuture<V> = ths.thenComposeAsync(map, executor).cast

    actual inline fun <V> thenComposeAsync(
        map: GameFunction<T, CompletableFuture<V>>, executor: Executor
    ): CompletableFuture<V> = ths.thenComposeAsync(map, executor).cast

    actual inline fun <V> thenComposeAsync(
        executor: Executor, noinline map: (T) -> CompletableFuture<V>
    ): CompletableFuture<V> = ths.thenComposeAsync(map, executor).cast

    actual inline fun <V> thenComposeAsync(
        executor: Executor, map: GameFunction<T, CompletableFuture<V>>
    ): CompletableFuture<V> = ths.thenComposeAsync(map, executor).cast
    //endregion

    //region Compat
    inline fun <V> thenApplyAsync(
        executor: JExecutor, noinline map: (T) -> V
    ): CompletableFuture<V> = ths.thenApplyAsync(map, executor).cast

    inline fun <V> thenApplyAsync(
        executor: JExecutor, map: GameFunction<in T, out V>
    ): CompletableFuture<V> = ths.thenApplyAsync(map, executor).cast

    inline fun <V> thenComposeAsync(
        executor: JExecutor, noinline map: (T) -> CompletableFuture<V>
    ): CompletableFuture<V> = ths.thenComposeAsync(map, executor).cast

    inline fun <V> thenComposeAsync(
        executor: JExecutor, map: GameFunction<T, CompletableFuture<V>>
    ): CompletableFuture<V> = ths.thenComposeAsync(map, executor).cast
    //endregion

    actual companion object {
        inline val <T> CompletableFuture<T>.ths: JCF<T>
            get() = this

        inline val <T> JCF<T>.cast: CompletableFuture<T>
            get() = this as CompletableFuture<T>

        actual fun allOf(futures: Collection<CompletableFuture<*>>): CompletableFuture<Unit> {
            return CompletableFuture<Unit>().also {
                JCF.allOf(*futures.toTypedArray()).whenComplete { _: Void?, throwable: Throwable? ->
                    if (throwable == null) it.complete(Unit)
                    else it.completeExceptionally(throwable)
                }
            }
        }

        actual fun allOf(vararg futures: CompletableFuture<*>): CompletableFuture<Unit> {
            return CompletableFuture<Unit>().also {
                JCF.allOf(*futures.toList().toTypedArray()).whenComplete { _: Void?, throwable: Throwable? ->
                        if (throwable == null) it.complete(Unit)
                        else it.completeExceptionally(throwable)
                    }
            }
        }
    }
}
