package de.dasbabypixel.gamelauncher.impl.vulkan

import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack

fun MemoryStack.UTF8Strings(strings: Collection<String>): PointerBuffer {
    return callocPointer(strings.size).apply {
        mark()
        for (string in strings) {
            put(UTF8(string))
        }
        reset()
    }
}
