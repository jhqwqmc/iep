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
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Command : ViCommand() {

    override fun execute(player: CommandSender, args: Array<out String>): Boolean {
        if (player !is Player) return false

        if (args.isEmpty()) {


            return true
        }

        when (args[0]) {
            "play" -> {
                if (Config.CONFIG.getBoolean("permissions") && !player.hasPermission("iep.play")) {
                    return true
                }

                PlayMenu.open(player)

                return true
            }
            "leaderboards" -> {
                if (Config.CONFIG.getBoolean("permissions") && !player.hasPermission("iep.leaderboard")) {
                    return true
                }

                LeaderboardMenu.open(player)

                return true
            }
            "settings" -> {
                val ep = player.asElytraPlayer() ?: return true

                if (ep.hasPermission("iep.setting")) {
                    return true
                }

                SettingsMenu.open(ep)
            }
            "leave" -> {
                val ep = player.asElytraPlayer() ?: return true

                if (player.hasPermission("iep.leave")) {
                    return true
                }

                ep.leave()
            }
            "reset" -> player.isInvulnerable = false
        }

        if (args.size > 1) {
            when (args[0]) {
                "seed" -> {
                    if (!Cooldowns.canPerform(player, "ep set seed", 1000)) {
                        return true
                    }

                    val iep = player.asElytraPlayer() ?: return true

                    try {
                        val seed = args[1].toInt()

                        if (seed < 0) throw NumberFormatException()

                        iep.getGenerator().set { settings -> Settings(settings, seed = seed) }
                        iep.getGenerator().reset(ResetReason.RESET, s = seed)

                        iep.send(Locales.getString(player, "settings.seed.set").replace("%a", args[1]))
                    } catch (ex: NumberFormatException) {
                        iep.send(Locales.getString(player, "settings.seed.invalid").replace("%a", args[1]))
                    }
                }
            }
        }

        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return emptyList()
    }
}
