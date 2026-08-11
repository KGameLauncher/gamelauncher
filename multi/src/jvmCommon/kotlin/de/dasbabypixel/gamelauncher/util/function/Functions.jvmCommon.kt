package de.dasbabypixel.gamelauncher.util.function

import de.dasbabypixel.gamelauncher.api.util.GameException
import java.util.concurrent.Callable

actual fun interface GameRunnable : Runnable, Function0<Unit> {
    @Throws(exceptionClasses = [GameException::class])
    actual override operator fun invoke()
    override fun run() = this()
}

actual fun interface GameCallable<T> : Callable<T>, Function0<T> {
    @Throws(GameException::class)
    actual override operator fun invoke(): T
    override fun call(): T = this()
}

actual fun interface GameConsumer<T> : Function1<T, Unit> {
    @Throws(GameException::class)
    actual override operator fun invoke(value: T)
}

actual fun interface GameBiConsumer<T, V> : Function2<T, V, Unit> {
    @Throws(GameException::class)
    actual override operator fun invoke(t: T, v: V)
}

actual fun interface GameFunction<T, V> : Function1<T, V> {
    @Throws(GameException::class)
    actual override operator fun invoke(value: T): V
}
