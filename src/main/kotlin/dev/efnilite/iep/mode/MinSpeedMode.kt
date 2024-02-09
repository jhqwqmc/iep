package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.generator.MinSpeedGenerator
import dev.efnilite.iep.generator.util.PointType
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material
import org.bukkit.entity.Player

object MinSpeedMode : Mode {

    override val name = "minspeed"

    override val leaderboard = Leaderboard(name)

    override fun create(player: Player) = Generator.create(player, leaderboard, PointType.CIRCLE) { MinSpeedGenerator() }

    override fun getItem(locale: String) = Item(Material.SPLASH_POTION, "<#cc3399><bold>Min Speed")
}