package de.dasbabypixel.gamelauncher.impl

import kotlin.reflect.KClass

class Providers {
    private val map = HashMap<KClass<*>, ClassEntry<*>>()
    private var frozen = false

    inline fun <reified T: Providable> provide(): T = provide(T::class)

    fun <T : Providable> provide(cls: KClass<in T>): T {
        return providers(cls).single()
    }

    fun <T : Providable> provide(cls: KClass<in T>, name: String): T {
        return providers(cls).get(name)
    }

    inline fun <reified T : Providable> provide(name: String) = provide(T::class, name)

    @Suppress("UNCHECKED_CAST")
    private fun <T : Providable> providers(cls: KClass<in T>): ClassEntry<T> {
        if (!frozen) throw IllegalStateException()
        return (map[cls]
            ?: throw ProviderNotRegisteredException("Missing provider with class ${cls.qualifiedName}")) as ClassEntry<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Providable> register(cls: KClass<in T>, name: String, provider: T) {
        if (frozen) throw IllegalStateException()
        val entry = map[cls] as ClassEntry<T>? ?: ClassEntry(cls).apply { map[cls] = this }
        entry.add(name, provider)
    }

    fun freeze() {
        frozen = true
    }
}

interface Providable

class ProviderNotRegisteredException(msg: String) : RuntimeException(msg)

inline fun <reified T : Providable> KClass<T>.provide(providers: Providers, name: String): T {
    return providers.provide(this, name)
}

inline fun <reified T : Providable> KClass<T>.provide(providers: Providers): T {
    return providers.provide(this)
}

inline fun <reified T : Providable> T.registerProvider(providers: Providers, name: String): T {
    providers.register(T::class, name, this)
    return this
}

private data class ClassEntry<T : Any>(val cls: KClass<in T>) {
    private val map = HashMap<String, T>()

    fun single(): T {
        if (map.size != 1) throw IllegalStateException("Multiple providers registered for ${cls.qualifiedName}")
        return map.values.first()
    }

    fun get(name: String): T {
        return map[name]
            ?: throw ProviderNotRegisteredException("Missing provider with class ${cls.qualifiedName} named $name")
    }

    fun add(name: String, value: T) {
        if (map.contains(name)) throw IllegalStateException("Provider with name $name already registered under class ${cls.qualifiedName}")
        map[name] = value
    }
}
