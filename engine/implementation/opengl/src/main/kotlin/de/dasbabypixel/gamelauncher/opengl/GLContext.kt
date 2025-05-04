package de.dasbabypixel.gamelauncher.opengl

interface GLContext {
    @Throws(IllegalStateException::class)
    fun acquire()
    fun release()
}