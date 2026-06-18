package de.dasbabypixel.gamelauncher.api.util.function

import de.dasbabypixel.gamelauncher.api.util.GameException

expect fun interface GameRunnable {
    @Throws(GameException::class)
    operator fun invoke()
}

expect fun interface GameCallable<T> {
    @Throws(GameException::class)
    operator fun invoke(): T
}

expect fun interface GameConsumer<T> {
    @Throws(GameException::class)
    operator fun invoke(value: T)
}

expect fun interface GameBiConsumer<T, V> {
    @Throws(GameException::class)
    operator fun invoke(t: T, v: V)
}

expect fun interface GameFunction<T, V> {
    @Throws(GameException::class)
    operator fun invoke(value: T): V
}
