package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.impl.api.util.concurrent.DisruptorMPSC

internal actual val EfficientMPSCs.instance: EfficientMPSC
    get() = DisruptorMPSC
