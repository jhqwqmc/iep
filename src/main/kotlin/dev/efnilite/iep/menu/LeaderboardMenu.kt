package dev.efnilite.iep.menu

import dev.efnilite.iep.ElytraPlayer
import dev.efnilite.iep.IEP
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.inventory.PagedMenu
import dev.efnilite.vilib.inventory.animation.RandomAnimation
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material

object LeaderboardMenu {

    fun open(player: ElytraPlayer) {
        val menu = Menu(3, "Leaderboards")
            .distributeRowsEvenly()
            .animation(RandomAnimation())

        for (mode in IEP.getModes()) {
            menu.item(menu.items.size + 9, mode.getItem("")
                .click({ SingleLeaderboardMenu.open(player, mode.leaderboard) })
            )
        }

        menu.open(player.player)
    }
}

private object SingleLeaderboardMenu {

    fun open(player: ElytraPlayer, leaderboard: Leaderboard) {
        val menu = PagedMenu(3, leaderboard.name)
            .displayRows(0, 1)

        for ((idx, score) in leaderboard.getAllScores().withIndex()) {
            menu.addToDisplay(
                listOf(
                    Item(Material.PLAYER_HEAD, "<white><bold>#${idx + 1} - ${score.name}")
                        .lore("<dark_gray>Score <white>${score.score}",
                            "<dark_gray>Time <white>${score.time}",
                            "<dark_gray>Seed <white>${score.seed}")
                )
            )
        }

        menu.prevPage(19, Item(Material.RED_DYE, "<white>Previous").click({ menu.page(-1) }))
            .nextPage(27, Item(Material.GREEN_DYE, "<white>Next").click({ menu.page(1) }))
            .item(22, Item(Material.ARROW, "<white><bold>Go back").click({ SettingsMenu.open(player) }))
            .open(player.player)
    }
}