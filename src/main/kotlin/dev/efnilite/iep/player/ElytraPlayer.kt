package dev.efnilite.iep.player

import dev.efnilite.iep.IEP
import dev.efnilite.iep.config.Config
import dev.efnilite.iep.config.Locales
import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.generator.Settings
import dev.efnilite.iep.leaderboard.Score.Companion.pretty
import dev.efnilite.iep.mode.Mode
import dev.efnilite.iep.reward.Reward
import dev.efnilite.iep.storage.Storage
import dev.efnilite.iep.world.Divider
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.fastboard.FastBoard
import dev.efnilite.vilib.util.Strings
import dev.efnilite.vilib.util.Task
import io.papermc.lib.PaperLib
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
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

    /**
     * Joins a [Mode].
     */
    fun join(mode: Mode) {
        IEP.log("Creating generator for ${player.name}, mode = ${mode.name}")

        val generator = mode.getGenerator()

        generator.add(this)

        val at = Divider.add(generator)

        data.setup(at)

        generator.start(mode, at, mode.pointType)
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
        PaperLib.teleportAsync(player, vector.toLocation(World.world))
    }

    /**
     * Teleports the player.
     * @param location The location to teleport to.
     */
    fun teleport(location: Location) {
        PaperLib.teleportAsync(player, location)
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
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(Strings.colour(message)))
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
        val local = line.replace("%score%", score.pretty())
            .replace("%high-score%", getGenerator().getHighScore().score.pretty())
            .replace("%time%", time)
            .replace("%seed%", seed.toString())

        return if (IEP.papiHook != null) {
            IEP.papiHook!!.replace(player, local)
        } else {
            local
        }
    }

    /**
     * Saves the player's settings.
     * @param settings The settings to save.
     */
    fun save(settings: Settings) {
        IEP.log("Saving settings for ${player.name}")

        if (IEP.stopping) {
            Storage.save(uuid, settings)
            return
        }

        Task.create(IEP.instance)
            .async()
            .execute { Storage.save(uuid, settings) }
            .run()
    }

    /**
     * Loads the player's settings.
     * @return The player's settings.
     */
    fun load(): Settings {
        IEP.log("Loading settings for $name")

        Storage.init(uuid)

        return Storage.load(uuid) ?: DEFAULT_SETTINGS
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