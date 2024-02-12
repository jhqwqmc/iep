package dev.efnilite.iep.generator

import dev.efnilite.iep.Config

class CloseGenerator : Generator() {

    override fun tick() {
        super.tick()

        val player = players[0]
        val pos = player.position
        val last = sections.minBy { it.key }
        val section = last.value
        val progressInSection = (score - (section.beginning.x - island.blockSpawn.x)).toInt()

        if (progressInSection < 0) {
            return
        }

        val isNear = section.isNearPoint(pos, progressInSection, RADIUS)

        if (score > 25 && !isNear) {
            reset()
        }
    }

    companion object {
        val RADIUS = Config.CONFIG.getDouble("mode-settings.close.radius") { it > 0 }
    }
}