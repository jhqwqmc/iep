package dev.efnilite.iep.player

import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.world.Divider
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.inventory.item.Item
import fr.mrmicky.fastboard.adventure.FastBoard
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.util.Vector

/**
 * Class for storing a player's previous data.
 */
private data class PreviousData(private val player: Player) {

    val gamemode = player.gameMode
    val position = player.location.toVector()
    val inventoryContents: Array<ItemStack?> = player.inventory.contents
    val effects: Collection<PotionEffect> = player.activePotionEffects

    /**
     * Sets player stuff.
     */
    fun join() {
        with(player) {
            clearActivePotionEffects()
            gameMode = GameMode.ADVENTURE
            isInvulnerable = true

            inventory.clear()

            inventory.chestplate = Item(Material.ELYTRA, "").unbreakable().build()
            inventory.addItem(Item(Material.FIREWORK_ROCKET, 64, "").build())
        }
    }

    /**
     * Resets the player's data.
     */
    fun leave() {
        with(player) {
            teleportAsync(position.toLocation(World.world))

            gameMode = gamemode
            inventory.contents = inventoryContents

            clearActivePotionEffects()
            addPotionEffects(effects)
        }
    }
}

/**
 * Class for wrapping players.
 */
class ElytraPlayer(private val player: Player) {

    /**
     * The player's position.
     */
    val position
        get() = player.location.toVector()

    /**
     * The player's previous position.
     */
    private val data = PreviousData(player)

    private val board = FastBoard(player)

    fun getGenerator(): Generator {
        return Divider.generators.first { it.players.contains(this) }
    }

    fun teleport(vector: Vector) {
        player.teleportAsync(vector.toLocation(World.world))
    }

    fun updateBoard(score: Int, time: String) {
        board.updateTitle(deserialize("<gradient:#ff0000:#800000>Infinite Elytra Parkour</gradient>"))

        board.updateLines(
            deserialize(""),
            deserialize("<#b30000><bold>Score</bold> <gray>$score"),
            deserialize("<#b30000><bold>Time</bold> <gray>$time"),
            deserialize(""),
            deserialize("<505050>server.ip")
        )
    }

    private fun deserialize(string: String): Component {
        return MiniMessage.miniMessage().deserialize(string)
    }

    /**
     * Joins the player to the generator.
     */
    fun join() = data.join()

    /**
     * Resets the data of the player.
     */
    fun leave() = data.leave()

    companion object {

        fun Player.asElytraPlayer(): ElytraPlayer? {
            return Divider.generators.flatMap { it.players }.firstOrNull { it.player == this }
        }
    }
}