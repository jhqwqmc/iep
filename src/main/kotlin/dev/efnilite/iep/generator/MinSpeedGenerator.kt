package dev.efnilite.iep.generator

import dev.efnilite.iep.Config
import kotlin.math.max

class MinSpeedGenerator : Generator() {

    private var startX = 0
    private var maxSpeed = 0.0
    private var ticksTooSlow = 0

    override fun getScore(): Double {
        if (startX == 0) return 0.0

        return max(0.0, players[0].position.x - startX)
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

        player.sendActionBar("${getProgressBar(speed, MIN_SPEED, ACTIONBAR_LENGTH)} <reset><dark_gray>| <gray>${getFormattedSpeed(player)}")
    }

    override fun reset(regenerate: Boolean, s: Int) {
        super.reset(regenerate, s)

        maxSpeed = 0.0
        ticksTooSlow = 0
        startX = 0
    }

    override fun resetPlayerHeight() {
        super.resetPlayerHeight()

        ticksTooSlow = 0
    }

    companion object {
        val MIN_SPEED = Config.CONFIG.getDouble("mode-settings.min-speed.min-speed") { it > 0 }
        val ACTIONBAR_LENGTH = Config.CONFIG.getInt("mode-settings.min-speed.actionbar-length") { it > 0 }
    }
}