package dev.efnilite.iep.menu

import dev.efnilite.iep.IEP
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.iep.player.ElytraPlayer
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.inventory.PagedMenu
import dev.efnilite.vilib.inventory.animation.RandomAnimation
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.Material

object LeaderboardMenu {

    fun open(player: ElytraPlayer) {
        val menu = Menu(3, "<white>Leaderboards")
            .distributeRowsEvenly()
            .animation(RandomAnimation())
            .fillBackground(Material.LIGHT_GRAY_STAINED_GLASS_PANE)

        for (leaderboard in IEP.getLeaderboards()) {
            menu.item(menu.items.size + 9, Item(Material.GOLD_BLOCK, "<white><bold>${leaderboard.name}")
                .click({ SingleLeaderboardMenu.open(player, leaderboard) }))
        }

        menu.open(player.player)
    }
}

private object SingleLeaderboardMenu {

    fun open(player: ElytraPlayer, leaderboard: Leaderboard) {
        val menu = PagedMenu(3, "<white>${leaderboard.name}")
            .displayRows(1, 2)

        for ((idx, score) in leaderboard.getAllScores().withIndex()) {
            with(score) {
                menu.addToDisplay(listOf(Item(Material.PLAYER_HEAD, "<white><bold>#${idx + 1} - $name")
                    .lore("<dark_gray>Score <white>$score",
                        "<dark_gray>Time <white>$time",
                        "<dark_gray>Seed <white>$seed")))
            }
        }

        menu.prevPage(19, Item(Material.RED_DYE, "<white>Previous"))
            .nextPage(27, Item(Material.GREEN_DYE, "<white>Next"))
            .item(22, Item(Material.ARROW, "<white>Go back"))
            .open(player.player)
    }
}