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
import dev.efnilite.vilib.schematic.Schematic
import dev.efnilite.vilib.util.Cooldowns
import dev.efnilite.vilib.util.Locations
import dev.efnilite.vilib.util.Strings
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.io.File
import java.util.*

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

                if (sender.isOp) {
                    send("<#7700FF>/iep schematic <pos1> <pos2> <dark_gray>- <gray>Saves the area between the positions")
                    send("<gray>⋅ Example: /iep schematic 0,0,0 -10,-10,-10")
                    send("")
                }
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
                "schematic" -> {
                    if (!sender.isOp) return true

                    try {
                        val pos1Nums = args[1].split(",").map { it.toInt() }
                        val pos2Nums = args[2].split(",").map { it.toInt() }

                        val pos1 = Vector(pos1Nums[0], pos1Nums[1], pos1Nums[2]).toLocation(sender.world)
                        val pos2 = Vector(pos2Nums[0], pos2Nums[1], pos2Nums[2]).toLocation(sender.world)

                        val uuid = UUID.randomUUID()
                        val file = File(IEP.instance.dataFolder, "schematics/$uuid")

                        sender.send("<gray>Saving your schematic as $uuid")

                        Schematic.create().save(file, pos1, pos2, IEP.instance)
                    } catch (ex: NumberFormatException) {
                        sender.send("<red>Invalid position format.")
                        return true
                    } catch (ex: IndexOutOfBoundsException) {
                        sender.send("<red>You need two positions to save the schematic.")
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

    private fun CommandSender.send(message: String) = sendMessage(Strings.colour(message))
}
