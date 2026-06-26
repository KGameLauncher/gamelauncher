@file:Suppress("NOTHING_TO_INLINE")

package de.dasbabypixel.gamelauncher.util.function

actual inline fun GameRunnable.asFunction(): () -> Unit = this

actual inline fun <T> GameCallable<T>.asFunction(): () -> T = this

actual inline fun <T> GameConsumer<T>.asFunction(): (T) -> Unit = this
actual inline fun <T, V> GameBiConsumer<T, V>.asFunction(): (T, V) -> Unit = this

actual inline fun <T, V> GameFunction<T, V>.asFunction(): (T) -> V = this
