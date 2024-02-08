package dev.efnilite.iep.generator

import dev.efnilite.iep.style.Style

/**
 * All settings.
 */
data class Settings(val style: Style, val radius: Int, val seed: Int) {

    constructor(settings: Settings,
                style: Style = settings.style,
                radius: Int = settings.radius,
                seed: Int = settings.seed) : this(style, radius, seed)

}