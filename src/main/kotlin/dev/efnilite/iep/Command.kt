package dev.efnilite.iep

import dev.efnilite.iep.ElytraPlayer.Companion.asElytraPlayer
import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.generator.util.Settings
import dev.efnilite.iep.menu.LeaderboardMenu
import dev.efnilite.iep.menu.PlayMenu
import dev.efnilite.iep.menu.SettingsMenu
import dev.efnilite.vilib.command.ViCommand
import dev.efnilite.vilib.mm.adventure.text.Component
import dev.efnilite.vilib.mm.adventure.text.minimessage.MiniMessage
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
                val iep = player.asElytraPlayer() ?: return true

                if (iep.hasPermission("iep.setting")) {
                    return true
                }

                SettingsMenu.open(iep)
            }
            "leave" -> {
                val iep = player.asElytraPlayer() ?: return true

                if (iep.hasPermission("iep.leave")) {
                    return true
                }

                Generator.remove(player)
            }
        }

        if (args.size > 1) {
            when (args[0]) {
                "seed" -> {
                    val iep = player.asElytraPlayer() ?: return true
                    val seed = args[1]

                    try {
                        iep.getGenerator().set { settings -> Settings(settings, seed = seed.toInt()) }
                        iep.send("<dark_gray>Seed set to <white>$seed.")
                    } catch (ex: NumberFormatException) {
                        iep.send("<white>$seed <dark_gray>is not a number.")
                        return true
                    }
                }
            }
        }

        return true
    }

    private fun deserialize(message: String): Component {
        return MiniMessage.miniMessage().deserialize(message)
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return emptyList()
    }
}
