package dev.efnilite.iep.menu

import dev.efnilite.iep.IEP
import dev.efnilite.iep.generator.Settings
import dev.efnilite.iep.player.ElytraPlayer
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material

object StylesMenu {

    fun open(player: ElytraPlayer) {
        val menu = Menu(3, "<white>Styles")
        val styles = IEP.getStyles()
        val generator = player.getGenerator()

        for ((idx, style) in styles.withIndex()) {
            menu.item(idx, Item(style.next(), "<white><bold>${style.name()}")
                .lore("<dark_gray>Type <white>${style.name().lowercase()}")
                .click({ generator.set { settings -> Settings(style, settings.radius) } }))
        }

        menu.item(21, Item(styles.random().next(), "<white><bold>Random")
            .click({ generator.set { settings -> Settings(styles.random(), settings.radius) } }))
            .item(23, Item(Material.ARROW, "<white><bold>Go back").click({ SettingsMenu.open(player) }))
            .open(player.player)
    }
}