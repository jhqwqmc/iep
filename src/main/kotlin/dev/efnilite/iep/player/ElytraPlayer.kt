package dev.efnilite.iep.player

import dev.efnilite.iep.IEP
import dev.efnilite.iep.config.Config
import dev.efnilite.iep.config.Locales
import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.generator.Settings
import dev.efnilite.iep.mode.Mode
import dev.efnilite.iep.reward.Reward
import dev.efnilite.iep.world.Divider
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.fastboard.FastBoard
import dev.efnilite.vilib.util.Strings
import dev.efnilite.vilib.util.Task
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.io.File
import java.util.concurrent.ThreadLocalRandom

/**
 * Class for wrapping players.
 */
class ElytraPlayer(val player: Player, private val data: PreviousData = PreviousData(player)) {

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
    private val board = FastBoard(player)
    private val file = File(IEP.instance.dataFolder, "players/${player.uniqueId}.json")

    /**
     * Joins a [Mode].
     */
    fun join(mode: Mode) {
        IEP.log("Creating generator for ${player.name}, mode = ${mode.name}")

        val generator = mode.getGenerator()

        generator.add(this)

        val at = Divider.add(generator)

        data.setup(at).thenRun {
            generator.start(mode, at, mode.pointType)
        }
    }

    /**
     * Leaves the current mode.
     */
    fun leave(switchMode: Boolean = false) {
        getGenerator().remove(this)

        data.reset(switchMode)

        if (switchMode) return

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
        player.sendMessage(message)
    }

    /**
     * Sends an action bar message to the player.
     * @param message The message to send.
     */
    fun sendActionBar(message: String) {
        player.sendActionBar(Strings.colour(message))
    }

    /**
     * Updates the player's board.
     * @param score The score to display.
     * @param time The time to display.
     * @param seed The seed to display.
     */
    fun updateBoard(score: Double, time: String, seed: Int) {
        board.updateTitle(Locales.getString(this, "scoreboard.title"))

        board.updateLines(Locales.getStringList(this, "scoreboard.lines")
            .map { updateLine(it, score, time, seed) })
    }

    private fun updateLine(line: String, score: Double, time: String, seed: Int): String {
        return line.replace("%score%", "%.1f".format(score))
            .replace("%high-score%", "%.1f".format(getGenerator().getHighScore().score))
            .replace("%time%", time)
            .replace("%seed%", seed.toString())
    }

    /**
     * Saves the player's settings.
     * @param settings The settings to save.
     */
    fun save(settings: Settings) {
        Task.create(IEP.instance)
            .async()
            .execute {
                IEP.log("Saving settings for ${player.name}")

                file.parentFile.mkdirs()
                file.createNewFile()

                file.writer().use { IEP.GSON.toJson(settings, it) }
            }
            .run()
    }

    /**
     * Loads the player's settings.
     * @return The player's settings.
     */
    fun load(): Settings {
        IEP.log("Loading settings for $name")

        if (!file.exists()) {
            return DEFAULT_SETTINGS
        }

        val serialized = file.reader().use { IEP.GSON.fromJson(it, Settings::class.java) }

        return serialized ?: DEFAULT_SETTINGS
    }

    fun addReward(mode: Mode, reward: Reward) {
        val set = data.leaveRewards[mode] ?: mutableSetOf()

        set.add(reward)

        data.leaveRewards[mode] = set
    }

    /**
     * @param permission The permission to check.
     * @return If the player has the permission.
     */
    fun hasPermission(permission: String): Boolean {
        if (Config.CONFIG.getBoolean("permissions")) {
            return player.hasPermission(permission)
        }

        return true
    }

    /**
     * Returns the generator the player is in.
     */
    fun getGenerator() = Divider.generators.first { it.players.contains(this) }

    companion object {

        val DEFAULT_SETTINGS
            get() = Settings(locale = Locales.getLocales().first(),
                metric = true,
                style = IEP.getStyles().first().name(),
                radius = 5,
                time = 0,
                seed = ThreadLocalRandom.current().nextInt(0, Generator.SEED_BOUND),
                info = false,
                fall = true,
                rewards = mutableSetOf())

        fun Player.asElytraPlayer(): ElytraPlayer? {
            return Divider.generators.flatMap { it.players }.firstOrNull { it.player.uniqueId == uniqueId }
        }
    }
}