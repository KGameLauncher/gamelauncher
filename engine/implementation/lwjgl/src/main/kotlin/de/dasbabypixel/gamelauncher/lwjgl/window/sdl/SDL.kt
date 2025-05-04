package de.dasbabypixel.gamelauncher.lwjgl.window.sdl

import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import org.lwjgl.sdl.SDLError

private object SDL {
    val logger = getLogger<SDL>()
}

fun checkError() {
    val error = SDLError.SDL_GetError()!!
    SDL.logger.error("SDL Error occurred: {}", error, Exception())
}