package dev.efnilite.iep

import dev.efnilite.iep.ElytraPlayer.Companion.asElytraPlayer
import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.generator.util.Settings
import dev.efnilite.iep.menu.PlayMenu
import dev.efnilite.iep.menu.SettingsMenu
import dev.efnilite.vilib.command.ViCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Command : ViCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.isEmpty() || sender !is Player) return false

        when (args[0]) {
            "play" -> PlayMenu.open(sender)
            "settings" -> {
                val player = sender.asElytraPlayer() ?: return true
                SettingsMenu.open(player)
            }
            "leave" -> Generator.remove(sender)
        }

        if (args.isNotEmpty()) {
            when (args[1]) {
                "seed" -> {
                    val player = sender.asElytraPlayer() ?: return true
                    val seed = args[2]

                    try {
                        player.getGenerator().set { settings -> Settings(settings, seed = seed.toInt()) }
                        player.send("<dark_gray>Seed set to <white>$seed.")
                    } catch (ex: NumberFormatException) {
                        player.send("<white>$seed <dark_gray>is not a number.")
                        return true
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
