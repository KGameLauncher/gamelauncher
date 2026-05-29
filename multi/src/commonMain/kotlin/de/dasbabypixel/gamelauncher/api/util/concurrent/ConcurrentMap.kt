package de.dasbabypixel.gamelauncher.api.util.concurrent

expect interface ConcurrentMap<K, V> : MutableMap<K, V>

expect fun <K, V> concurrentHashMap(): ConcurrentMap<K, V>
expect fun <E> concurrentSet(): MutableSet<E>
