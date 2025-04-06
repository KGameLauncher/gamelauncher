package de.dasbabypixel.gamelauncher.lwjgl

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.lwjgl.lifecycle.InitLifecycle
import de.dasbabypixel.gamelauncher.lwjgl.lifecycle.ShutdownLifecycle
import de.dasbabypixel.gamelauncher.impl.provider.registerProvider

class LWJGLGameLauncher : GameLauncher() {
    init {
        registerProvider<GameLauncher>("game_launcher")
    }

    override fun start() {
        InitLifecycle.init()
    }

    override fun stop() {
        ShutdownLifecycle.shutdown()
    }

    override fun handleException(ex: Throwable) {
        ShutdownLifecycle.shutdown(ex)
    }
}
