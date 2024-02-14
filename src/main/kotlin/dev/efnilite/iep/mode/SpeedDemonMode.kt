package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.SpeedDemonGenerator
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material

object SpeedDemonMode : Mode {

    override val name = "speed demon"

    override val leaderboard = Leaderboard(name)

    override fun getGenerator() = SpeedDemonGenerator()

    override fun getItem(locale: String): Item = Item(Material.SPLASH_POTION, "<#cc3399><bold>Speed Demon")
        .lore("<gray>Go as fast as you can.")
}