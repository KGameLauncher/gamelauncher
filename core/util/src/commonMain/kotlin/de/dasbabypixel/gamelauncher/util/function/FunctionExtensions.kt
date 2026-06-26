package de.dasbabypixel.gamelauncher.util.function

fun <T> GameCallable<T>.toRunnable(): GameRunnable = object : GameRunnable {
    override fun invoke() {
        this@toRunnable()
    }

    override fun toString(): String = this@toRunnable.toString()
}

fun GameRunnable.toCallable(): GameCallable<Unit> = object : GameCallable<Unit> {
    override fun invoke() = this@toCallable.invoke()

    override fun toString(): String = this@toCallable.toString()
}

expect inline fun GameRunnable.asFunction(): () -> Unit

expect inline fun <T> GameCallable<T>.asFunction(): () -> T

expect inline fun <T> GameConsumer<T>.asFunction(): (T) -> Unit
expect inline fun <T, V> GameBiConsumer<T, V>.asFunction(): (T, V) -> Unit

expect inline fun <T, V> GameFunction<T, V>.asFunction(): (T) -> V
