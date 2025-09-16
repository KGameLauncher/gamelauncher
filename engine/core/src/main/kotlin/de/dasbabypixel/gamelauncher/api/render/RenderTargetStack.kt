package de.dasbabypixel.gamelauncher.api.render

class RenderTargetStack {
    private val stack = ArrayList<RenderTarget>()

    fun push(target: RenderTarget) {
        stack.addLast(target)
    }

    val current: RenderTarget
        get() = stack.last()

    fun pop(): RenderTarget {
        return stack.removeLast()
    }
}