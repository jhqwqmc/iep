package dev.efnilite.iep.menu

import dev.efnilite.iep.IEP
import dev.efnilite.iep.config.Locales
import dev.efnilite.iep.player.ElytraPlayer
import dev.efnilite.iep.player.ElytraPlayer.Companion.asElytraPlayer
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.util.Cooldowns
import org.bukkit.entity.Player

object PlayMenu {

    fun open(player: Player) {
        val menu = Menu(3, Locales.getString(player, "play.title"))
            .item(23, Locales.getItem(player, "go back").click({ player.closeInventory() }))
            .distributeRowsEvenly()

        for (mode in IEP.getModes()) {
            menu.item(9 + menu.items.size, mode.getItem(player)
                .click({
                    if (!Cooldowns.canPerform(player, "iep join", 2500)) {
                        return@click
                    }

                    val ep = player.asElytraPlayer()

                    if (ep == null) {
                        ElytraPlayer(player).join(mode)
                    } else {
                        ep.leave(true)

                        ep.join(mode)
                    }
                }))
        }

        menu.open(player)
    }

}