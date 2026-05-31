package de.dasbabypixel.gamelauncher.api.util

import de.dasbabypixel.gamelauncher.api.util.concurrent.ConcurrentMap
import de.dasbabypixel.gamelauncher.api.util.concurrent.concurrentHashMap

object Config {
    private val config: ConcurrentMap<String, ConfigValue<out Any>> = concurrentHashMap()

    val name = createString("name", "GameLauncher")
    val inIDE = createBoolean("in_ide", false)
    val debug = createBoolean("debug", inIDE)
    val trackResources = createBoolean("track_resources", debug)
    val calculateThreadStacks = createBoolean("calculate_thread_stacks", debug)

    fun <T : Any> named(name: String): ConfigValue<T> {
        @Suppress("UNCHECKED_CAST") return (config[name]
            ?: throw IllegalStateException("No config with name $name registered")) as ConfigValue<T>
    }

    fun <T : Any> create(name: String, defaultValue: T): ConfigValue<T> {
        return create(name, defaultValue, defaultValue)
    }

    fun <T : Any> create(name: String, defaultValue: ConfigValue<T>): ConfigValue<T> {
        return create(name, defaultValue.value)
    }

    fun createString(name: String, defaultValue: String): ConfigValue<String> {
        return create(name, defaultValue, systemPropertyString(name))
    }

    fun createString(name: String, defaultValue: ConfigValue<String>): ConfigValue<String> {
        return createString(name, defaultValue.value)
    }

    fun createBoolean(name: String, defaultValue: Boolean): ConfigValue<Boolean> {
        return create(name, defaultValue, systemPropertyBoolean(name))
    }

    fun createBoolean(name: String, defaultValue: ConfigValue<Boolean>): ConfigValue<Boolean> {
        return createBoolean(name, defaultValue.value)
    }

    fun createInt(name: String, defaultValue: Int): ConfigValue<Int> {
        return create(name, defaultValue, systemPropertyInt(name))
    }

    fun createInt(name: String, defaultValue: ConfigValue<Int>): ConfigValue<Int> {
        return create(name, defaultValue.value)
    }

    private fun <T : Any> create(name: String, defaultValue: T, value: T?): ConfigValue<T> {
        val c = ConfigValue(name, defaultValue, value ?: defaultValue)
        if (config.putIfAbsent(name, c) != null) {
            throw IllegalStateException("Config with name $name already registered")
        }
        return c
    }

    class ConfigValue<T>(val name: String, val defaultValue: T, value: T) {
        var value: T = value
            private set

        init {
            if (name.lowercase() != name) throw IllegalArgumentException("Name must be namespaced lowercase")
        }

        fun reset() {
            value = defaultValue
        }

        operator fun invoke(): T = value
    }
}

internal expect fun Config.systemPropertyString(name: String): String?
internal expect fun Config.systemPropertyInt(name: String): Int?
internal expect fun Config.systemPropertyBoolean(name: String): Boolean?
