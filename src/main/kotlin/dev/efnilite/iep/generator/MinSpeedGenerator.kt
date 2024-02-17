package dev.efnilite.iep.generator

import dev.efnilite.iep.config.Config
import org.bukkit.util.Vector
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

        player.sendActionBar("${getProgressBar(speed)}${getAboveBar(speed)} <reset><dark_gray>| " +
                "<gray>${convertSpeed(speed, true)}/${convertSpeed(MIN_SPEED, true)} km/h <dark_gray>| " +
                "<gray>${convertSpeed(speed, false)}/${convertSpeed(MIN_SPEED, false)} mph")
    }

    private fun getAboveBar(speed: Double): String {
        val barAmount = ((MIN_SPEED - speed) / INCREMENTS).toInt()

        return (0 until barAmount).joinToString("") { "<red><bold>|" }
    }

    private fun getProgressBar(t: Double): String {
        return (0 until ACTIONBAR_LENGTH)
            .map { if (it * INCREMENTS < t) return@map "<red><bold>|" else return@map "<reset><dark_gray>|" }
            .joinToString("") { it }
    }


    override fun reset(regenerate: Boolean, s: Int, overrideSeedSettings: Boolean) {
        super.reset(regenerate, s, overrideSeedSettings)

        maxSpeed = 0.0
        ticksTooSlow = 0
        startX = 0
    }

    override fun resetPlayerHeight(toStart: Vector) {
        super.resetPlayerHeight(toStart)

        ticksTooSlow = 0
    }

    companion object {
        val MIN_SPEED = Config.CONFIG.getDouble("mode-settings.min-speed.min-speed") { it > 0 }
        val ACTIONBAR_LENGTH = Config.CONFIG.getInt("mode-settings.min-speed.actionbar-length") { it > 0 }
        val INCREMENTS = MIN_SPEED / ACTIONBAR_LENGTH
    }
}