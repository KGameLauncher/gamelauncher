@file:Suppress("UNCHECKED_CAST") @file:JvmSynthetic

package de.dasbabypixel.gamelauncher.impl.provider

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object Providers {
    private val map = ConcurrentHashMap<KClass<*>, ClassEntry<*>>()

    fun <T : Providable> provider(cls: KClass<in T>): T {
        return providers(cls).single()
    }

    fun <T : Providable> provider(cls: KClass<in T>, name: String): T {
        return providers(cls).get(name)
    }

    private fun <T : Providable> providers(cls: KClass<in T>): ClassEntry<T> {
        return (map[cls]
            ?: throw ProviderNotRegisteredException("Missing provider with class ${cls.qualifiedName}")) as ClassEntry<T>
    }

    fun <T : Providable> register(cls: KClass<in T>, name: String, provider: T) {
        (map.computeIfAbsent(cls) { ClassEntry(cls.java) } as ClassEntry<T>).add(name, provider)
    }
}

interface Providable

class ProviderNotRegisteredException(msg: String) : RuntimeException(msg)

inline fun <reified T : Providable> provide(): T {
    return T::class.provide()
}

inline fun <reified T : Providable> provide(name: String): T {
    return T::class.provide(name)
}

inline fun <reified T : Providable> KClass<T>.provide(name: String): T {
    return Providers.provider(this, name)
}

inline fun <reified T : Providable> KClass<T>.provide(): T {
    return Providers.provider(this)
}

inline fun <reified T : Providable> T.registerProvider(name: String): T {
    Providers.register(T::class, name, this)
    return this
}

private data class ClassEntry<T>(val cls: Class<T>) {
    private val map = ConcurrentHashMap<String, T>()

    fun single(): T {
        if (map.size != 1) throw IllegalStateException("Multiple providers registered for ${cls.name}")
        return map.values.first()
    }

    fun get(name: String): T {
        return map[name] ?: throw ProviderNotRegisteredException("Missing provider with class ${cls.name} named $name")
    }

    fun add(name: String, value: T) {
        val existing = map.putIfAbsent(name, value)
        if (existing != null) throw IllegalStateException("Provider with name $name already registered under class ${cls.name}")
    }
}