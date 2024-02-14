package dev.efnilite.iep.menu

import dev.efnilite.iep.config.Config
import dev.efnilite.iep.IEP
import dev.efnilite.iep.config.Locales
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.inventory.PagedMenu
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta

object LeaderboardMenu {

    fun open(player: Player) {
        val menu = Menu(3, "Leaderboards")
            .distributeRowsEvenly()
            .item(23, Locales.getItem(player, "go-back").click({ player.inventory.close() }))

        for (mode in IEP.getModes()) {
            if (Config.CONFIG.getBoolean("permissions") && !player.hasPermission("iep.leaderboard.${mode.name}")) {
                continue
            }

            menu.item(menu.items.size + 9, mode.getItem("")
                .click({ SingleLeaderboardMenu.open(player, mode.leaderboard) })
            )
        }

        menu.open(player.player)
    }
}

private object SingleLeaderboardMenu {

    fun open(player: Player, leaderboard: Leaderboard) {
        val menu = PagedMenu(3, leaderboard.name.toTitleCase())
            .displayRows(0, 1)

        for ((idx, entry) in leaderboard.getAllScores().withIndex()) {
            val (uuid, score) = entry

            val item = Item(Material.PLAYER_HEAD, "<white><bold>#${idx + 1} - ${score.name}")
                .lore("<gray>Score <white>${"%.1f".format(score.score)}",
                    "<gray>Time <white>${score.getFormattedTime()}",
                    "<gray>Seed <white>${score.seed}")

            val meta = item.build().itemMeta
            (meta as SkullMeta).owningPlayer = Bukkit.getOfflinePlayer(uuid)
            item.meta(meta)

            menu.addToDisplay(listOf(item))

            if (uuid == player.uniqueId) {
                menu.item(21, item.clone())
            }
        }

        menu
            .prevPage(19, Item(Material.RED_DYE, "<white>Previous").click({ menu.page(-1) }))
            .nextPage(27, Item(Material.GREEN_DYE, "<white>Next").click({ menu.page(1) }))
            .distributeRowEvenly(2)
            .item(23, Locales.getItem(player, "go-back").click({ LeaderboardMenu.open(player) }))
            .open(player.player)
    }

    private fun String.toTitleCase(): String {
        return this.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}