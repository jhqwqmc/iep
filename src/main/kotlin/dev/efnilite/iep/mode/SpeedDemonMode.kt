package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.generator.SpeedDemonGenerator
import dev.efnilite.iep.generator.util.PointType
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material
import org.bukkit.entity.Player

object SpeedDemonMode : Mode {

    override val name = "speed demon"

    override val leaderboard = Leaderboard(name)

    override fun create(player: Player) = Generator.create(player, leaderboard) { SpeedDemonGenerator() }

    override fun getItem(locale: String): Item = Item(Material.SPLASH_POTION, "<#cc3399><bold>Speed Demon")
        .lore("<gray>Go as fast as you can.")
}