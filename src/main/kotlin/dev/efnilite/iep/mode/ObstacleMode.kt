package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.ObstacleGenerator
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material

object ObstacleMode : Mode {

    override val name = "obstacle"

    override val leaderboard = Leaderboard(name)

    override fun getGenerator() = ObstacleGenerator()

    override fun getItem(locale: String): Item = Item(Material.OAK_FENCE, "<#c49402><bold>Obstacle")
        .lore("<gray>Occasional obstacles.")
}