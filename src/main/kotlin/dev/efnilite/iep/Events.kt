package dev.efnilite.iep

import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.player.ElytraPlayer.Companion.asElytraPlayer
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.event.EventWatcher
import dev.efnilite.vilib.util.Task
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class Events : EventWatcher {

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
    fun right(event: PlayerInteractEvent) {
        val player = event.player.asElytraPlayer() ?: return

        if (event.action != Action.RIGHT_CLICK_AIR) return

        if (event.item?.type != Material.FIREWORK_ROCKET) return

        Task.create(IEP.instance)
            .delay(Config.CONFIG.getInt("firework-respawn-time") { it >= 0 } * 20)
            .execute { player.player.inventory.addItem(event.item!!) }
            .run()
    }
}