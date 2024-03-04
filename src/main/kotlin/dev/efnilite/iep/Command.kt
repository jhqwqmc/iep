package dev.efnilite.iep

import dev.efnilite.iep.config.Config
import dev.efnilite.iep.config.Locales
import dev.efnilite.iep.generator.ResetReason
import dev.efnilite.iep.generator.Settings
import dev.efnilite.iep.menu.LeaderboardMenu
import dev.efnilite.iep.menu.PlayMenu
import dev.efnilite.iep.menu.SettingsMenu
import dev.efnilite.iep.player.ElytraPlayer.Companion.asElytraPlayer
import dev.efnilite.vilib.command.ViCommand
import dev.efnilite.vilib.util.Cooldowns
import dev.efnilite.vilib.util.Strings
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Command : ViCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player || args.isEmpty()) {
            with(sender) {
                send("")
                send("<dark_gray>= <gradient:#4800FF:#B200FF><bold>Infinite Elytra Parkour</bold></gradient> <dark_gray>=")
                send("")
                send("<#7700FF>/iep play <dark_gray>- <gray>Opens the play menu")
                send("<#7700FF>/iep leaderboards <dark_gray>- <gray>Opens the leaderboards menu")
                send("<#7700FF>/iep settings <dark_gray>- <gray>Opens the settings menu")
                send("<#7700FF>/iep leave <dark_gray>- <gray>Leaves the game")
                send("<#7700FF>/iep seed <seed> <dark_gray>- <gray>Set the current seed")
                send("")
            }

            return true
        }

        when (args[0]) {
            "play" -> {
                if (Config.CONFIG.getBoolean("permissions") && !sender.hasPermission("iep.play")) {
                    return true
                }

                PlayMenu.open(sender)

                return true
            }
            "leaderboards" -> {
                if (Config.CONFIG.getBoolean("permissions") && !sender.hasPermission("iep.leaderboard")) {
                    return true
                }

                LeaderboardMenu.open(sender)

                return true
            }
            "settings" -> {
                val ep = sender.asElytraPlayer() ?: return true

                if (ep.hasPermission("iep.setting")) {
                    return true
                }

                SettingsMenu.open(ep)
            }
            "leave" -> {
                val ep = sender.asElytraPlayer() ?: return true

                if (sender.hasPermission("iep.leave")) {
                    return true
                }

                ep.leave()
            }
        }

        if (args.size > 1) {
            when (args[0].lowercase()) {
                "seed" -> {
                    if (!Cooldowns.canPerform(sender, "iep set seed", 1000)) {
                        return true
                    }

                    val iep = sender.asElytraPlayer() ?: return true

                    try {
                        val seed = args[1].toInt()

                        if (seed < 0) throw NumberFormatException()

                        iep.getGenerator().set { settings -> Settings(settings, seed = seed) }
                        iep.getGenerator().reset(ResetReason.RESET, s = seed)

                        iep.send(Locales.getString(sender, "settings.seed.set").replace("%a", args[1]))
                    } catch (ex: NumberFormatException) {
                        iep.send(Locales.getString(sender, "settings.seed.invalid").replace("%a", args[1]))
                    }
                }
            }
        }

        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return emptyList()
    }

    private fun CommandSender.send(message: String) = sendMessage(Strings.colour(message))
}
