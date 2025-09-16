package de.dasbabypixel.gamelauncher.opengl

import de.dasbabypixel.gamelauncher.gles.GLES32
import de.dasbabypixel.gamelauncher.impl.provider.Providable
import de.dasbabypixel.gamelauncher.impl.provider.Providers

object GLProvider {
    val GLES: GLES32 = Providers.provider<ProvidableGL>("gles").gl
}

class ProvidableGL(val gl: GLES32) : Providable