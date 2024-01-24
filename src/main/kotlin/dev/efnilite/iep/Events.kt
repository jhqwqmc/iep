package dev.efnilite.iep

import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.menu.SettingsMenu
import dev.efnilite.iep.player.ElytraPlayer.Companion.asElytraPlayer
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.event.EventWatcher
import dev.efnilite.vilib.util.Task
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class Events : EventWatcher {

    @EventHandler
    fun drop(event: PlayerDropItemEvent) {
        val player = event.player.asElytraPlayer() ?: return

        event.isCancelled = true
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        val player = event.player.asElytraPlayer() ?: return

        player.getGenerator().remove(player)
    }

    @EventHandler
    fun change(event: PlayerChangedWorldEvent) {
        val player = event.player.asElytraPlayer() ?: return

        if (event.player.world == World.world) return

        player.getGenerator().remove(player)
    }

    @EventHandler
    fun join(event: PlayerJoinEvent) {
        if (Config.CONFIG.getBoolean("join-on-join")) {
            Generator.create(event.player)
        }
    }

    @EventHandler
    fun rightRocket(event: PlayerInteractEvent) {
        val player = event.player.asElytraPlayer() ?: return

        if (event.action != Action.RIGHT_CLICK_AIR || event.hand != EquipmentSlot.HAND) return

        if (event.item?.type != Material.FIREWORK_ROCKET) return

        Task.create(IEP.instance)
            .delay(Config.CONFIG.getInt("firework-respawn-time") { it >= 0 } * 20)
            .execute { player.player.inventory.addItem(ItemStack(Material.FIREWORK_ROCKET)) }
            .run()
    }

    @EventHandler
    fun rightSettings(event: PlayerInteractEvent) {
        val player = event.player.asElytraPlayer() ?: return

        if (event.action != Action.RIGHT_CLICK_AIR || event.hand != EquipmentSlot.HAND) return

        if (event.item?.type != Material.STRING) return

        SettingsMenu.open(player)
    }
}