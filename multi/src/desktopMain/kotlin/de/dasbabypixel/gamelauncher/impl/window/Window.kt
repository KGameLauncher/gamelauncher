package de.dasbabypixel.gamelauncher.impl.window

import de.dasbabypixel.gamelauncher.api.resource.GameResource
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKSurface

interface Window : GameResource {
    val surface: VKSurface

    /**
     * Schedules the window to be shown. Because of OS constraints, this operation may not be immediate. Waiting for the future is safe.
     */
    fun show(): CompletableFuture<Unit>

    /**
     * Schedules the window to be hidden. Because of OS constraints, this operation may not be immediate. Waiting for the future is safe.
     */
    fun hide(): CompletableFuture<Unit>

    /**
     * The last known value whether the window is visible or not.
     * Is not updated right after shown, but rather after the future returned by [show] returns
     */
    val isVisible: Boolean

    /**
     * Fetches the visibility to mitigate OS-Specific behavior. Running this after [show] or [hide], even without waiting on their future, will return the expected value
     */
    fun fetchVisible(): CompletableFuture<Boolean>

    fun fetchMaximized(): CompletableFuture<Boolean>

    fun fetchIconified(): CompletableFuture<Boolean>
}
