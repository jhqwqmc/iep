package dev.efnilite.iep

import dev.efnilite.iep.generator.util.Settings
import dev.efnilite.iep.world.Divider
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.ViPlugin
import dev.efnilite.vilib.inventory.item.Item
import fr.mrmicky.fastboard.adventure.FastBoard
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.GameMode
import org.bukkit.Location
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
            inventory.addItem(Item(Material.SUGAR_CANE, "<#2fb900><bold>Play").build())
            inventory.addItem(Item(Material.COMPARATOR, "<#c10000><bold>Settings").build())
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
class ElytraPlayer(val player: Player) {

    /**
     * The player's position.
     */
    val position
        get() = player.location.toVector()

    val name = player.name
    val uuid = player.uniqueId

    /**
     * The player's previous position.
     */
    private val data = PreviousData(player)

    private val board = FastBoard(player)

    /**
     * Joins the player to the generator.
     */
    fun join() = data.join()

    /**
     * Resets the data of the player.
     */
    fun leave() {
        data.leave()

        board.delete()
    }

    /**
     * Returns the generator the player is in.
     */
    fun getGenerator() = Divider.generators.first { it.players.contains(this) }

    /**
     * Teleports the player.
     * @param vector The vector to teleport to.
     */
    fun teleport(vector: Vector) {
        player.teleportAsync(vector.toLocation(World.world))
    }

    /**
     * Teleports the player.
     * @param location The location to teleport to.
     */
    fun teleport(location: Location) {
        player.teleportAsync(location)
    }

    /**
     * Sends a message to the player.
     * @param message The message to send.
     */
    fun send(message: String) {
        player.sendMessage(deserialize(message))
    }

    /**
     * Sends an action bar message to the player.
     * @param message The message to send.
     */
    fun sendActionBar(message: String) {
        player.sendActionBar(deserialize(message))
    }

    /**
     * Updates the player's board.
     * @param score The score to display.
     * @param time The time to display.
     * @param seed The seed to display.
     */
    fun updateBoard(score: Int, time: String, seed: Int) {
        board.updateTitle(deserialize("<gradient:#ff0000:#800000><bold>IEP</gradient>"))

        board.updateLines(
            deserialize(""),
            deserialize("<#b30000><bold>Score</bold> <gray>$score"),
            deserialize("<#b30000><bold>Time</bold> <gray>$time"),
            deserialize("<#b30000><bold>Seed</bold> <gray>$seed"),
            deserialize(""),
            deserialize("<#505050>server.ip")
        )
    }

    fun save(settings: Settings) {
        ViPlugin.getGson().toJson(settings)
    }

    private fun deserialize(string: String): Component {
        return MiniMessage.miniMessage().deserialize(string)
    }

    companion object {

        fun Player.asElytraPlayer(): ElytraPlayer? {
            return Divider.generators.flatMap { it.players }.firstOrNull { it.player == this }
        }
    }
}