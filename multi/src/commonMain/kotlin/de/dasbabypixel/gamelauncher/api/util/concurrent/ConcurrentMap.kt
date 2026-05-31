package de.dasbabypixel.gamelauncher.api.util.concurrent

expect interface ConcurrentMap<K, V> : MutableMap<K, V>

expect fun <K : Any, V : Any> concurrentHashMap(): ConcurrentMap<K, V>
expect fun <E : Any> concurrentSet(): MutableSet<E>
