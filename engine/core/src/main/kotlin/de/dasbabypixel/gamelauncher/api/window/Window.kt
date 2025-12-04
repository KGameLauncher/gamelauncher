package de.dasbabypixel.gamelauncher.api.window

import de.dasbabypixel.gamelauncher.api.util.concurrent.FrameSync
import java.util.concurrent.CompletableFuture

interface Window {
    val frameSync: FrameSync
    val renderThread: WindowRenderThread
    fun requestFocus(): CompletableFuture<Unit>
    fun forceFocus(): CompletableFuture<Unit>
    fun show(): CompletableFuture<Unit>
    fun hide(): CompletableFuture<Unit>
}