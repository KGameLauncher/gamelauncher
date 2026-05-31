package de.dasbabypixel.gamelauncher.api.util

import de.dasbabypixel.gamelauncher.api.util.Config.createBoolean
import de.dasbabypixel.gamelauncher.api.util.Config.createString
import de.dasbabypixel.gamelauncher.api.util.Config.inIDE

object DesktopConfig {
    val windowSystem = createString("window_system", "sdl")
    val useAnsi = createBoolean("use_ansi", inIDE.value || System.console() != null)
}

internal actual fun Config.systemPropertyString(name: String): String? {
    return System.getProperty("gamelauncher.$name")
}

internal actual fun Config.systemPropertyInt(name: String): Int? {
    val prop = systemPropertyString(name)
    return prop?.toIntOrNull()
}

internal actual fun Config.systemPropertyBoolean(name: String): Boolean? {
    val prop = systemPropertyString(name)
    if (prop == "true" || prop == "1") return true
    if (prop == "false" || prop == "0") return false
    return null
}
