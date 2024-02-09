package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.CloseGenerator
import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.generator.util.PointType
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material
import org.bukkit.entity.Player

object CloseMode : Mode {

    override val name = "close"

    override val leaderboard = Leaderboard(name)

    override fun create(player: Player) = Generator.create(player, leaderboard, PointType.FLAT) { CloseGenerator() }

    override fun getItem(locale: String) = Item(Material.PINK_PETALS, "<#fa9abc><bold>Close")
}