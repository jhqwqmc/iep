package dev.efnilite.iep.menu

import dev.efnilite.iep.IEP
import dev.efnilite.iep.player.ElytraPlayer
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.inventory.item.Item

object SettingsMenu {

    fun open(player: ElytraPlayer) {
        val menu = Menu(3, "<white>Settings")

        menu.item(9, Item(IEP.getStyles().random().next(), "<gradient:#8c0000:#e60000><bold>Style")
            .click( { StylesMenu.open(player) } ))

        menu.distributeRowsEvenly()
            .open(player.player)
    }
}