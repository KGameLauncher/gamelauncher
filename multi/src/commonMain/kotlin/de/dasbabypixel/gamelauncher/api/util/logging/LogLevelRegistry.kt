package de.dasbabypixel.gamelauncher.api.util.logging

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
object LogLevelRegistry {
    private val levels = ArrayList<LogType>()
    private val customLevels = HashMap<String, LogType.Fixed>()

    private val frozen = AtomicBoolean(false)

    init {
        levels.add(LogType.Default)
        levels.add(LogType.Stdout)
        levels.add(LogType.Stderr)
    }

    fun registerLevel(level: LogType.Fixed) {
        if (frozen.load()) throw IllegalStateException("LevelRegistry is frozen")
        if (level.marker.isEmpty()) throw IllegalArgumentException("LogLevel with empty marker is not allowed")
        if (customLevels.contains(level.marker)) throw IllegalStateException("Level with marker ${level.marker} already registered")
        levels.add(level)
        customLevels[level.marker] = level
    }

    fun levels(): List<LogType> {
        frozen.store(true)
        return levels
    }
}
