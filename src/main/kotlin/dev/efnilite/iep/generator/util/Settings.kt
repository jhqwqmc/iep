package dev.efnilite.iep.generator.util

import dev.efnilite.iep.style.Style

/**
 * All settings.
 */
data class Settings(val style: Style, val radius: Int, val seed: Int, val info: Boolean) {

    constructor(settings: Settings,
                style: Style = settings.style,
                radius: Int = settings.radius,
                seed: Int = settings.seed,
                info: Boolean = settings.info) : this(style, radius, seed, info)

}