package dev.efnilite.iep.generator

import dev.efnilite.iep.Config
import kotlin.math.max

class MinSpeedGenerator : Generator() {

    private var startX = 0
    private var maxSpeed = 0.0
    private var ticksTooSlow = 0

    override val score: Int
        get() {
            if (startX == 0) return 0

            return max(0, (players[0].position.x - startX).toInt())
        }

    override fun tick() {
        super.tick()

        val player = players[0]
        val speed = getSpeed(player)

        maxSpeed = maxOf(maxSpeed, speed)

        if (speed > MIN_SPEED && startX == 0) {
            startX = player.position.blockX
        }

        if (MIN_SPEED > speed && maxSpeed > MIN_SPEED) {
            ticksTooSlow++
        }

        if (ticksTooSlow == 3) {
            reset()
        }

        player.sendActionBar("${getProgressBar(speed)} <reset><dark_gray>| <gray>${getFormattedSpeed(player)}")
    }

    override fun reset(regenerate: Boolean) {
        super.reset(regenerate)

        maxSpeed = 0.0
        ticksTooSlow = 0
        startX = 0
    }

    override fun resetPlayerHeight() {
        super.resetPlayerHeight()

        ticksTooSlow = 0
    }

    private fun getProgressBar(speed: Double): String {
        val increments = MIN_SPEED / ACTIONBAR_LENGTH.toDouble()

        return (0 until ACTIONBAR_LENGTH)
            .map {
                if (it * increments < speed) {
                    return@map "<green><bold>|"
                } else {
                    return@map "<reset><dark_gray>|"
                }
            }
            .joinToString("") { it }
    }

    companion object {
        val MIN_SPEED = Config.CONFIG.getInt("mode-settings.min-speed.min-speed")
        val ACTIONBAR_LENGTH = Config.CONFIG.getInt("mode-settings.min-speed.actionbar-length")
    }
}