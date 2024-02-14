package dev.efnilite.iep.menu

import dev.efnilite.iep.IEP
import dev.efnilite.iep.config.Locales
import dev.efnilite.iep.generator.util.Settings
import dev.efnilite.iep.player.ElytraPlayer
import dev.efnilite.iep.style.RandomStyle
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material

object StylesMenu {

    fun open(player: ElytraPlayer) {
        val menu = Menu(3, "Styles")
        val styles = IEP.getStyles()
        val generator = player.getGenerator()

        for ((idx, style) in styles.withIndex()) {
            if (!player.hasPermission("iep.setting.style.${style.name()}")) {
                continue
            }

            menu.item(idx, Item(style.next(), "<white><bold>${style.name()}")
                    .lore("<gray>Type <white>${if (style is RandomStyle) "random" else "incremental"}")
                    .click({
                        generator.set { settings -> Settings(settings, style = style) }
                        player.player.inventory.close()
                    }))
        }

        menu.item(21, Item(styles.random().next(), "<white><bold>Random")
                .click({
                    generator.set { settings -> Settings(settings, style = styles.random()) }
                    player.player.inventory.close()
                }))
            .item(23, Locales.getItem(player.player, "go-back").click({ SettingsMenu.open(player) }))
            .open(player.player)
    }
}