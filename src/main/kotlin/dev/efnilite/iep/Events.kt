package dev.efnilite.iep

import dev.efnilite.iep.config.Config
import dev.efnilite.iep.menu.LeaderboardMenu
import dev.efnilite.iep.menu.PlayMenu
import dev.efnilite.iep.menu.SettingsMenu
import dev.efnilite.iep.mode.DefaultMode
import dev.efnilite.iep.player.ElytraPlayer
import dev.efnilite.iep.player.ElytraPlayer.Companion.asElytraPlayer
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.event.EventWatcher
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot

class Events : EventWatcher {

    @EventHandler
    fun drop(event: PlayerDropItemEvent) {
        val player = event.player.asElytraPlayer() ?: return

        event.isCancelled = true
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        val player = event.player.asElytraPlayer() ?: return

        player.leave()
    }

    @EventHandler
    fun change(event: PlayerChangedWorldEvent) {
        val player = event.player.asElytraPlayer() ?: return

        if (event.player.world == World.world) return

        player.leave()
    }

    @EventHandler
    fun join(event: PlayerJoinEvent) {
        if (Config.CONFIG.getBoolean("join-on-join")) {
            ElytraPlayer(event.player).join(DefaultMode)
        }
    }

    @EventHandler
    fun rightRocket(event: BlockPlaceEvent) {
        val player = event.player.asElytraPlayer() ?: return

        event.isCancelled = true
    }

    @EventHandler
    fun rightSettings(event: PlayerInteractEvent) {
        val player = event.player.asElytraPlayer() ?: return

        if ((event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) ||
            event.hand != EquipmentSlot.HAND) return

        when (event.item?.type) {
            Material.SUGAR_CANE -> {
                if (player.hasPermission("iep.play")) {
                    PlayMenu.open(player.player)
                }
            }
            Material.COMPARATOR -> {
                if (player.hasPermission("iep.setting")) {
                    SettingsMenu.open(player)
                }
            }
            Material.SPRUCE_HANGING_SIGN -> {
                if (player.hasPermission("iep.leaderboard")) {
                    LeaderboardMenu.open(player.player)
                }
            }

            else -> {}
        }
    }
}