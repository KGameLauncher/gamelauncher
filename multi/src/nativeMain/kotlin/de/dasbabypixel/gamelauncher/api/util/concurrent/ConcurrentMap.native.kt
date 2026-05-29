package de.dasbabypixel.gamelauncher.api.util.concurrent

actual interface ConcurrentMap<K, V> : MutableMap<K, V>

actual fun <K, V> concurrentHashMap(): ConcurrentMap<K, V> = TODO("no concurrent maps in native...")
actual fun <E> concurrentSet(): MutableSet<E> {
    TODO("Not yet implemented")
}