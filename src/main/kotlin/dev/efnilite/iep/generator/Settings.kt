package dev.efnilite.iep.generator

import dev.efnilite.iep.IEP
import dev.efnilite.iep.style.Style

/**
 * All settings.
 */
data class Settings(val locale: String,
                    val metric: Boolean,
                    val style: String,
                    val radius: Int,
                    val time: Int,
                    val seed: Int,
                    val info: Boolean,
                    val rewards: MutableSet<Int>) {

    constructor(settings: Settings,
                locale: String = settings.locale,
                metric: Boolean = settings.metric,
                style: String = settings.style,
                radius: Int = settings.radius,
                time: Int = settings.time,
                seed: Int = settings.seed,
                info: Boolean = settings.info,
                rewards: MutableSet<Int> = settings.rewards) :

            this(locale = locale,
                metric = metric,
                style = style,
                radius = radius,
                time = time,
                seed = seed,
                info = info,
                rewards = rewards)

    companion object {

        fun String.asStyle(): Style {
            return IEP.getStyle(this)
        }
    }

}