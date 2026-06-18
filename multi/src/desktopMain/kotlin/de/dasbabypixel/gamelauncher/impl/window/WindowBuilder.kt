package de.dasbabypixel.gamelauncher.impl.window

import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture

interface WindowBuilder {
    fun initialPosition(x: Int, y: Int)
    fun initialSize(width: Int, height: Int)
    fun initialResizable(resizable: Boolean)
    fun build(): CompletableFuture<Window>
}
