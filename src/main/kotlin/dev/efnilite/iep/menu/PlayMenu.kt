package dev.efnilite.iep.menu

import dev.efnilite.iep.IEP
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material
import org.bukkit.entity.Player

object PlayMenu {

    fun open(player: Player) {
        val menu = Menu(3, "Play")
            .item(23, Item(Material.ARROW, "<white><bold>Go back").click({ player.inventory.close() }))
            .distributeRowsEvenly()

        for (mode in IEP.getModes()) {
            menu.item(9 + menu.items.size, mode.getItem("")
                .click({ mode.create(player) }))
        }

        menu.open(player)
    }

}