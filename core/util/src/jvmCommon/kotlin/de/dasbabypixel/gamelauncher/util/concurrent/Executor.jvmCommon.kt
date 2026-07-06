package de.dasbabypixel.gamelauncher.util.concurrent

import de.dasbabypixel.gamelauncher.util.function.GameCallable
import de.dasbabypixel.gamelauncher.util.function.GameRunnable
import de.dasbabypixel.gamelauncher.util.function.toCallable

actual interface Executor : java.util.concurrent.Executor {
    actual fun submit(runnable: GameRunnable): CompletableFuture<Unit> =
        submit(runnable.toCallable())

    actual fun <T> submit(callable: GameCallable<T>): CompletableFuture<T>
    actual fun submitGR(runnable: GameRunnable): CompletableFuture<Unit> = submit(runnable)

    actual fun <T> submitGC(callable: GameCallable<T>): CompletableFuture<T> = submit(callable)

    override fun execute(p0: Runnable) {
        submit { p0.run() }
    }
}
