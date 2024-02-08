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
            .distributeRowsEvenly()
            .fillBackground(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
        val generator = player.getGenerator()

        menu.item(9, Item(IEP.getStyles().random().next(), "<gradient:#8c0000:#e60000><bold>Style")
            .click({ StylesMenu.open(player) }))

        menu.item(
            10, SliderItem()
                .initial(generator.settings.radius - 3)
                .add(3, Item(Material.GREEN_DYE, "<#2eb82e><bold>Radius 6")
                ) {
                    generator.set { settings -> Settings(settings, radius = 6) }
                    return@add true
                }
                .add(2, Item(Material.YELLOW_DYE, "<#ffff00><bold>Radius 5")
                ) {
                    generator.set { settings -> Settings(settings, radius = 5) }
                    return@add true
                }
                .add(1, Item(Material.ORANGE_DYE, "<#e68a00><bold>Radius 4")
                ) {
                    generator.set { settings -> Settings(settings, radius = 4) }
                    return@add true
                }
                .add(0, Item(Material.RED_DYE, "<#cc3300><bold>Radius 3")
                ) {
                    generator.set { settings -> Settings(settings, radius = 3) }
                    return@add true
                }
        )

        menu.item(11, Item(Material.WRITABLE_BOOK, "<white><bold>Leaderboards")
            .click({ LeaderboardMenu.open(player) }))

        menu.open(player.player)
    }
}