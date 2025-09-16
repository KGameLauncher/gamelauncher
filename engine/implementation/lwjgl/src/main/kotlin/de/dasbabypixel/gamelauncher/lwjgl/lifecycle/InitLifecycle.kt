package de.dasbabypixel.gamelauncher.lwjgl.lifecycle

import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.extension.getBoolean
import de.dasbabypixel.gamelauncher.lwjgl.started
import de.dasbabypixel.gamelauncher.lwjgl.util.logging.LWJGLLogging
import org.lwjgl.system.Configuration
import java.util.*

object InitLifecycle {
    fun init() {
        superEarlyInit()
        if (Debug.debug) {
            Configuration.DEBUG.set(true)
            Configuration.DEBUG_MEMORY_ALLOCATOR.set(true)
            Configuration.DEBUG_STACK.set(false)
            Configuration.DEBUG_FUNCTIONS.set(true)
        } else {
            Configuration.DEBUG.set(false)
            Configuration.DISABLE_CHECKS.set(true)
        }
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
