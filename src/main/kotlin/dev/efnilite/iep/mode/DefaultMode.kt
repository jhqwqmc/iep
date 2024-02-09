package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.generator.util.PointType
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material
import org.bukkit.entity.Player

object DefaultMode : Mode {

    override val name = "default"

    override val leaderboard = Leaderboard(name)

    override fun create(player: Player) = Generator.create(player, leaderboard, PointType.CIRCLE) { Generator() }

    override fun getItem(locale: String) = Item(Material.BARREL, "<white><bold>Default")
}