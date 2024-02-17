package dev.efnilite.iep.generator

import dev.efnilite.iep.style.Style

/**
 * All settings.
 */
data class Settings(val locale: String,
                    val metric: Boolean,
                    val style: Style,
                    val radius: Int,
                    val time: Int,
                    val seed: Int,
                    val info: Boolean) {

    constructor(settings: Settings,
                locale: String = settings.locale,
                metric: Boolean = settings.metric,
                style: Style = settings.style,
                radius: Int = settings.radius,
                time: Int = settings.time,
                seed: Int = settings.seed,
                info: Boolean = settings.info) :

            this(locale = locale,
                metric = metric,
                style = style,
                radius = radius,
                time = time,
                seed = seed,
                info = info)

}