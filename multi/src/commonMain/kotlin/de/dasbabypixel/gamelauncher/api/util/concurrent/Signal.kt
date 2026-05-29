package de.dasbabypixel.gamelauncher.api.util.concurrent

expect class Signal {
    constructor()

    fun await()

    fun signal()
}
