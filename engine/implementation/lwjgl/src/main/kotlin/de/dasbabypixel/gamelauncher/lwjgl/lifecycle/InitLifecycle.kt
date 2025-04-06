package de.dasbabypixel.gamelauncher.lwjgl.lifecycle

import de.dasbabypixel.gamelauncher.api.util.extension.getBoolean
import de.dasbabypixel.gamelauncher.lwjgl.started
import de.dasbabypixel.gamelauncher.lwjgl.util.logging.LWJGLLogging
import java.util.*

object InitLifecycle {
    fun init() {
        superEarlyInit()
        LWJGLLogging.init()
        started()
    }

    private fun superEarlyInit() {
        if (!Boolean.getBoolean("gamelauncher.skipsysprops")) {
            val props = (this.javaClass.classLoader.getResourceAsStream("gamelauncher.sysprops")
                ?: throw IllegalStateException("Missing gamelauncher.sysprops")).use {
                val props = Properties()
                props.load(it)
                props
            }
            props.forEach {
                System.setProperty(it.key.toString(), it.value.toString())
            }
        }
    }
}
