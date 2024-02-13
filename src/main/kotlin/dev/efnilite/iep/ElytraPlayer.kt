package dev.efnilite.iep

import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.generator.util.Settings
import dev.efnilite.iep.world.Divider
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.inventory.item.Item
import dev.efnilite.vilib.util.Task
import fr.mrmicky.fastboard.adventure.FastBoard
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.util.Vector
import java.io.File
import java.util.concurrent.ThreadLocalRandom

private data class SerializedSettings(val style: String, val radius: Int, val seed: Int, val info: Boolean) {

    constructor(settings: Settings) : this(settings.style.name(), settings.radius, settings.seed, settings.info)

    fun convert() = Settings(IEP.getStyles().first { it.name() == style }, radius, seed, info)

}

/**
 * Class for storing a player's previous data.
 */
private data class PreviousData(private val player: Player) {

    val invulnerable = player.isInvulnerable
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
            inventory.addItem(Item(Material.SPRUCE_HANGING_SIGN, "<white><bold>Leaderboard").build())
        }
    }

    /**
     * Resets the player's data.
     */
    fun leave() {
        with(player) {
            teleportAsync(position.toLocation(World.world))

            isInvulnerable = invulnerable
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
    private val file = File(IEP.instance.dataFolder, "players/${player.uniqueId}.json")

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
    fun updateBoard(score: Double, time: String, seed: Int) {
        board.updateTitle(deserialize("<gradient:#ff0000:#800000><bold>IEP</gradient>"))

        board.updateLines(
            deserialize(""),
            deserialize("<#b30000><bold>Score</bold> <gray>${"%.1f".format(score)}"),
            deserialize("<#b30000><bold>Time</bold> <gray>$time"),
            deserialize("<#b30000><bold>Seed</bold> <gray>$seed"),
            deserialize(""),
            deserialize("<#505050>server.ip")
        )
    }

    /**
     * Saves the player's settings.
     * @param settings The settings to save.
     */
    fun save(settings: Settings) {
        Task.create(IEP.instance)
            .async()
            .execute {
                file.parentFile.mkdirs()
                file.createNewFile()

                file.writer().use { IEP.GSON.toJson(SerializedSettings(settings), it) }
            }
            .run()
    }

    /**
     * Loads the player's settings.
     * @return The player's settings.
     */
    fun load(): Settings {
        if (!file.exists()) {
            return DEFAULT_SETTINGS
        }

        val serialized = file.reader().use { IEP.GSON.fromJson(it, SerializedSettings::class.java) }

        return serialized?.convert() ?: DEFAULT_SETTINGS
    }

    /**
     * Returns the generator the player is in.
     */
    fun getGenerator() = Divider.generators.first { it.players.contains(this) }

    private fun deserialize(string: String) = MiniMessage.miniMessage().deserialize(string)

    companion object {

        val DEFAULT_SETTINGS
            get() = Settings(IEP.getStyles()[0], 5, ThreadLocalRandom.current().nextInt(0, Generator.SEED_BOUND), false)

        fun Player.asElytraPlayer(): ElytraPlayer? {
            return Divider.generators.flatMap { it.players }.firstOrNull { it.player == this }
        }
    }
}