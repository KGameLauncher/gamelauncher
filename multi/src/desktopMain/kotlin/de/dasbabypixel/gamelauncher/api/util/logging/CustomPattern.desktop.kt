package de.dasbabypixel.gamelauncher.api.util.logging

import de.dasbabypixel.gamelauncher.api.util.logging.log4j.Log4jPatternPlatformProvider

actual val CustomPattern.Companion.platform: CustomPattern.PlatformProvider
    get() = Log4jPatternPlatformProvider