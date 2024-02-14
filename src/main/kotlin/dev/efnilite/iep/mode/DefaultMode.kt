package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material

object DefaultMode : Mode {

    override val name = "default"

    override val leaderboard = Leaderboard(name)

    override fun getGenerator() = Generator()

    override fun getItem(locale: String) = Item(Material.BARREL, "<white><bold>Default")
}