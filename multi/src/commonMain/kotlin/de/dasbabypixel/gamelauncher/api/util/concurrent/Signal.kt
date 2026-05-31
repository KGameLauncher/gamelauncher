package de.dasbabypixel.gamelauncher.api.util.concurrent

interface Signal {
    fun await()

    fun signal()

    companion object {
        operator fun invoke(): Signal = createSignal()
    }
}

internal expect fun Signal.Companion.createSignal(): Signal
