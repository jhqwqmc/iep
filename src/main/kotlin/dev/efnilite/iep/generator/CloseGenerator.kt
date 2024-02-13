package dev.efnilite.iep.generator

import dev.efnilite.iep.Config

class CloseGenerator : Generator() {

    override fun tick() {
        super.tick()

        val player = players[0]
        val pos = player.position
        val section = sections.minBy { it.key }.value
        val progressInSection = pos.x - section.beginning.x

        if (progressInSection < 0) {
            return
        }

        val isNear = section.isNearPoint(pos, progressInSection.toInt(), RADIUS)

        if (getScore() > 25 && !isNear) {
            reset()
        }
    }

    companion object {
        val RADIUS = Config.CONFIG.getDouble("mode-settings.close.radius") { it > 0 }
    }
}