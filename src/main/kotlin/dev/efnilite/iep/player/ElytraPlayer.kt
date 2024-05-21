package dev.efnilite.iep.player

import com.google.common.io.ByteStreams
import dev.efnilite.iep.IEP
import dev.efnilite.iep.config.Config
import dev.efnilite.iep.config.Locales
import dev.efnilite.iep.generator.Settings
import dev.efnilite.iep.leaderboard.Score.Companion.pretty
import dev.efnilite.iep.mode.Mode
import dev.efnilite.iep.reward.Reward
import dev.efnilite.iep.storage.Storage
import dev.efnilite.iep.world.Divider
import dev.efnilite.vilib.fastboard.FastBoard
import dev.efnilite.vilib.util.Strings
import dev.efnilite.vilib.util.Task
import io.papermc.lib.PaperLib
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.ChannelNotRegisteredException
import java.util.concurrent.CompletableFuture

/**
 * Class for wrapping players.
 */
class ElytraPlayer(val player: Player, private val data: PreviousData = PreviousData(player)) : IElytraPlayer {

    override val position
        get() = player.location.toVector()

    override val name = player.name
    override val uuid = player.uniqueId

    private var boardTitle = ""
    private var boardLines = listOf<String>()
    private val board = FastBoard(player)

    /**
     * Joins a [Mode].
     */
    override fun join(mode: Mode) {
        IEP.log("Creating generator for ${player.name} with mode ${mode.name}")

        val generator = mode.getGenerator()

        val at = Divider.add(generator)

        generator.add(this)

        data.setup(at).thenRun { generator.start(mode, at, mode.pointType) }
    }

    /**
     * Leaves the current mode.
     */
    override fun leave(switchMode: Boolean, urgent: Boolean) {
        getGenerator().remove(this)

        if (!switchMode && Config.CONFIG.getBoolean("proxy.enabled")) {
            sendToServer(Config.CONFIG.getString("proxy.return-server"))

            return
        }

        data.reset(switchMode, urgent)

        if (switchMode) return

        board.delete()
    }

    private fun sendToServer(server: String) {
        IEP.log("Sending ${player.name} to proxy server $server")

        val out = ByteStreams.newDataOutput()
        out.writeUTF("Connect")
        out.writeUTF(server)

        try {
            player.sendPluginMessage(IEP.instance, "BungeeCord", out.toByteArray())

            IEP.log("Sent ${player.name} to proxy server $server")
        } catch (ex: ChannelNotRegisteredException) {
            IEP.logging.stack("$server is not registered on proxy", ex)
            player.kickPlayer("$server is not registered on proxy")
        }
    }

    /**
     * Loads the player's settings.
     * @return The player's settings.
     */
    override fun load(): Settings {
        IEP.log("Loading settings for $name")

        Storage.init(uuid)

        return Storage.load(uuid) ?: DEFAULT_SETTINGS
    }

    /**
     * Saves the player's settings.
     * @param settings The settings to save.
     */
    override fun save(settings: Settings) {
        IEP.log("Saving settings for ${player.name}")

        updateBoardValues()
        player.setPlayerTime(settings.time.toLong(), false)

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
     * Sends a message to the player.
     * @param message The message to send.
     */
    override fun send(message: String) {
        player.sendMessage(message)
    }

    /**
     * Sends an action bar message to the player.
     * @param message The message to send.
     */
    override fun sendActionBar(message: String) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(Strings.colour(message)))
    }

    /**
     * Adds a reward to the player's settings.
     */
    override fun addReward(mode: Mode, reward: Reward) {
        val set = data.leaveRewards[mode] ?: mutableSetOf()

        set += reward

        data.leaveRewards[mode] = set
    }

    /**
     * Updates the player's board.
     * @param score The score to display.
     * @param time The time to display.
     * @param seed The seed to display.
     */
    fun updateBoard(score: Double, time: String, seed: Int) {
        if (boardTitle.isEmpty()) {
            updateBoardValues()
        }

        board.updateTitle(boardTitle)

        board.updateLines(boardLines.map { updateLine(it, score, time, seed) })
    }

    // saves 6% performance!
    private fun updateBoardValues() {
        boardTitle = Locales.getString(this, "scoreboard.title")
        boardLines = Locales.getStringList(this, "scoreboard.lines")
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
     * Teleports the player.
     * @param location The location to teleport to.
     */
    fun teleport(location: Location): CompletableFuture<Boolean> {
        return PaperLib.teleportAsync(player, location)
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
    fun getGenerator() = Divider.generators.first { it.player == this }

    companion object {

        val DEFAULT_SETTINGS
            get() = Settings(locale = Config.CONFIG.getString("settings.locale.default"),
                metric = Config.CONFIG.getBoolean("settings.metric.default"),
                style = Config.CONFIG.getString("settings.style.default"),
                radius = Config.CONFIG.getInt("settings.radius.default") { it in 3..6 },
                time = Config.CONFIG.getInt("settings.time.default") { it in 0..<24000 },
                seed = Config.CONFIG.getInt("settings.seed.default") { it in -1..1_000_000 },
                info = Config.CONFIG.getBoolean("settings.info.default"),
                fall = Config.CONFIG.getBoolean("settings.fall.default"),
                rewards = mutableSetOf())

        fun Player.asElytraPlayer(): ElytraPlayer? {
            return Divider.generators.map { it.player }.firstOrNull { it.player == this }
        }
    }
}