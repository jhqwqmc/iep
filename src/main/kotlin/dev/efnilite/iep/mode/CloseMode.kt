package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.CloseGenerator
import dev.efnilite.iep.generator.util.PointType
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material

object CloseMode : Mode {

    override val name = "close"

    override val leaderboard = Leaderboard(name)

    override val pointType = PointType.FLAT

    override fun getGenerator() = CloseGenerator()

    override fun getItem(locale: String): Item = Item(Material.PINK_PETALS, "<#fa9abc><bold>Close")
        .lore("<gray>Stay close to the blocks.")
}