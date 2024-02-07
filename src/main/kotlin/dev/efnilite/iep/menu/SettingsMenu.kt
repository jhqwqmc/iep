package dev.efnilite.iep.menu

import dev.efnilite.iep.IEP
import dev.efnilite.iep.generator.Settings
import dev.efnilite.iep.player.ElytraPlayer
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.inventory.item.Item
import dev.efnilite.vilib.inventory.item.SliderItem
import org.bukkit.Material

object SettingsMenu {

    fun open(player: ElytraPlayer) {
        val menu = Menu(3, "<white>Settings")
        val settings = player.getGenerator().settings

        menu.item(9, Item(IEP.getStyles().random().next(), "<gradient:#8c0000:#e60000><bold>Style")
            .click({ StylesMenu.open(player) }))

        menu.item(
            10, SliderItem()
                .initial(settings.radius)
                .add(5, Item(Material.GREEN_WOOL, "<#2eb82e>Radius 5")
                ) {
                    settings.radius = 5
                    return@add true
                }
        )

        menu.distributeRowsEvenly()
            .open(player.player)
    }

    private fun set(setting: Settings, radius: Int) {

    }
}