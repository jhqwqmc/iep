package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.MinSpeedGenerator
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material

object MinSpeedMode : Mode {

    override val name = "min speed"

    override val leaderboard = Leaderboard(name)

    override fun getGenerator() = MinSpeedGenerator()

    override fun getItem(locale: String): Item = Item(Material.FEATHER, "<white><bold>Min Speed")
        .lore("<gray>Once you've reached the minimum speed,", "<gray>going slower will reset you.")
}