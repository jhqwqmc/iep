package dev.efnilite.iep.menu

import dev.efnilite.iep.IEP
import dev.efnilite.iep.generator.Settings
import dev.efnilite.iep.player.ElytraPlayer
import dev.efnilite.iep.style.Style
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material

object StylesMenu {

    fun open(player: ElytraPlayer) {
        val menu = Menu(3, "<white>Styles")
        val styles = IEP.getStyles()

        for ((idx, style) in styles.withIndex()) {
            menu.item(idx, Item(style.next(), "<#4d0000><bold>${style.name()}")
                .lore("<dark_gray>Type ${style.name().lowercase()}")
                .click({ setStyle(player, style) }))
        }

        menu.item(21, Item(styles.random().next(), "<gradient:#8c0000:#e60000><bold>Random")
            .click({ setStyle(player, styles.random()) }))
            .item(23, Item(Material.ARROW, "<#800000><bold>Go back").click({ SettingsMenu.open(player) }))
            .open(player.player)
    }

    private fun setStyle(player: ElytraPlayer, style: Style) {
        val settings = player.getGenerator().settings

        player.getGenerator().settings = Settings(style, settings.radius)

        player.player.closeInventory()
    }
}