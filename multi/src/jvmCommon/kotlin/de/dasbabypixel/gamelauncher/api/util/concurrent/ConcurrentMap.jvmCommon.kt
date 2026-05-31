package de.dasbabypixel.gamelauncher.api.util.concurrent

import java.util.concurrent.ConcurrentHashMap

actual typealias ConcurrentMap<K, V> = CMap<K, V>

interface CMap<K, V> : MutableMap<K, V>, java.util.concurrent.ConcurrentMap<K, V> {
    override fun remove(key: K & Any, value: V & Any): Boolean

    override fun putIfAbsent(key: K & Any, value: V & Any): V?

    override fun replace(key: K & Any, oldValue: V & Any, newValue: V & Any): Boolean

    override fun replace(key: K & Any, value: V & Any): V?
}

private class CHMap<K : Any, V : Any> : ConcurrentHashMap<K, V>(), CMap<K, V>

actual fun <K : Any, V : Any> concurrentHashMap(): ConcurrentMap<K, V> = CHMap()

actual fun <E : Any> concurrentSet(): MutableSet<E> = ConcurrentHashMap.newKeySet<E>()
