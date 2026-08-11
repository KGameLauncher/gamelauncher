package de.dasbabypixel.gamelauncher.util.concurrent

interface Signal {
    fun await()

    fun signal()

    companion object {
        operator fun invoke(): Signal = createSignal()
    }
}

internal expect fun Signal.Companion.createSignal(): Signal
