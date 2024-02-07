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
                .initial(generator.settings.radius)
                .add(6, Item(Material.GREEN_WOOL, "<#2eb82e><bold>Radius 6")
                ) {
                    generator.set { settings -> Settings(settings.style, 6) }
                    return@add true
                }
                .add(5, Item(Material.YELLOW_WOOL, "<#ffff00><bold>Radius 5")
                ) {
                    generator.set { settings -> Settings(settings.style, 5) }
                    return@add true
                }
                .add(4, Item(Material.ORANGE_WOOL, "<#e68a00><bold>Radius 4")
                ) {
                    generator.set { settings -> Settings(settings.style, 4) }
                    return@add true
                }
                .add(3, Item(Material.RED_WOOL, "<#cc3300><bold>Radius 3")
                ) {
                    generator.set { settings -> Settings(settings.style, 3) }
                    return@add true
                }
        )

        menu.item(11, Item(Material.WRITABLE_BOOK, "<white>Leaderboards")
            .click({ LeaderboardMenu.open(player) }))

        menu.open(player.player)
    }
}