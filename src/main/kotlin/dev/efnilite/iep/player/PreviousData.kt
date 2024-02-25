package dev.efnilite.iep.player

import dev.efnilite.iep.config.Locales
import dev.efnilite.iep.mode.Mode
import dev.efnilite.iep.reward.Reward
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.inventory.item.Item
import io.papermc.lib.PaperLib
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.util.Vector
import java.util.concurrent.CompletableFuture

/**
 * Class for storing a player's previous data.
 */
data class PreviousData(private val player: Player) {

    val leaveRewards: MutableMap<Mode, MutableSet<Reward>> = mutableMapOf()

    private val gamemode = player.gameMode
    private val position = player.location
    private val inventoryContents: Array<ItemStack?> = player.inventory.contents
    private val effects: Collection<PotionEffect> = player.activePotionEffects

    /**
     * Sets player stuff.
     */
    fun setup(vector: Vector): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        with(player) {
            PaperLib.teleportAsync(this, vector.toLocation(World.world)).thenRun {
                resetPlayerTime()
                activePotionEffects.forEach { removePotionEffect(it.type) }

                gameMode = GameMode.ADVENTURE
                isInvulnerable = true

                inventory.clear()

                inventory.chestplate = Item(Material.ELYTRA, "").unbreakable().build()
                inventory.addItem(Locales.getItem(this, "hotbar.play").build())
                inventory.addItem(Locales.getItem(this, "hotbar.settings").build())
                inventory.addItem(Locales.getItem(this, "hotbar.leaderboards").build())
                inventory.addItem(Locales.getItem(this, "hotbar.leave").build())

                future.complete(true)
            }
        }

        return future
    }

    /**
     * Resets the player's data.
     */
    fun reset(switchMode: Boolean) {
        if (switchMode) {
            reset()
        } else {
            PaperLib.teleportAsync(player, position).thenRun { reset() }
        }
    }

    private fun reset() {
        with(player) {
            isInvulnerable = false
            gameMode = gamemode
            inventory.contents = inventoryContents

            activePotionEffects.forEach { removePotionEffect(it.type) }
            addPotionEffects(effects)

            resetPlayerTime()
        }

        for ((mode, rewards) in leaveRewards.toMap()) {
            rewards.forEach { it.execute(player, mode) }
        }
    }
}