package dev.efnilite.iep.player

import dev.efnilite.iep.world.World
import dev.efnilite.vilib.inventory.item.Item
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.util.Vector

/**
 * Class for storing a player's previous data.
 */
data class PreviousData(private val player: Player) {

    private val gamemode = player.gameMode
    private val position = player.location
    private val inventoryContents: Array<ItemStack?> = player.inventory.contents
    private val effects: Collection<PotionEffect> = player.activePotionEffects

    /**
     * Sets player stuff.
     */
    fun setup(vector: Vector) {
        with(player) {
            teleportAsync(vector.toLocation(World.world)).thenRun {
                clearActivePotionEffects()
                gameMode = GameMode.ADVENTURE
                isInvulnerable = true

                inventory.clear()

                inventory.chestplate = Item(Material.ELYTRA, "").unbreakable().build()
                inventory.addItem(Item(Material.SUGAR_CANE, "<#2fb900><bold>Play").build())
                inventory.addItem(Item(Material.COMPARATOR, "<#c10000><bold>Settings").build())
                inventory.addItem(Item(Material.SPRUCE_HANGING_SIGN, "<white><bold>Leaderboard").build())
            }
        }
    }

    /**
     * Resets the player's data.
     */
    fun reset(switchMode: Boolean) {
        val reset = {
            with(player) {
                isInvulnerable = false
                gameMode = gamemode
                inventory.contents = inventoryContents

                clearActivePotionEffects()
                addPotionEffects(effects)
            }
        }

        if (switchMode) {
            reset()
        } else {
            player.teleportAsync(position).thenRun { reset() }
        }
    }
}