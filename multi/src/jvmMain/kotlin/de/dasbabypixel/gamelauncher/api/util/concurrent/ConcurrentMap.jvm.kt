package de.dasbabypixel.gamelauncher.api.util.concurrent

import java.util.concurrent.ConcurrentHashMap

actual typealias ConcurrentMap<K, V> = java.util.concurrent.ConcurrentMap<K, V>

actual fun <K, V> concurrentHashMap(): ConcurrentMap<K, V> = ConcurrentHashMap()
actual fun <E> concurrentSet(): MutableSet<E> = ConcurrentHashMap.newKeySet<E>()
