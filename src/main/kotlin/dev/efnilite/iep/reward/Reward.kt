package dev.efnilite.iep.reward

import dev.efnilite.iep.IEP
import dev.efnilite.iep.hook.VaultHook
import dev.efnilite.iep.mode.Mode
import dev.efnilite.iep.player.ElytraPlayer
import org.bukkit.Bukkit

/**
 * String representation of a reward.
 *
 * Format: <give-time>||<mode>||<command>||<value>
 */
data class Reward(val string: String) {

    fun execute(player: ElytraPlayer, currentMode: Mode) {
        val parts = string.split("||", limit = 4)

        val time = parts[0].lowercase()

        if (time == "leave") {
            player.addReward(Reward("now||${parts[1]}||${parts[2]}||${parts[3]}"))
            return
        }

        val modeName = parts[1].lowercase()

        if (modeName != "all") {
            val mode = IEP.getMode(modeName)

            if (mode == null) {
                IEP.instance.logging.error("Invalid mode $currentMode in rewards")
                return
            }

            if (mode != currentMode) {
                return
            }
        }

        val command = parts[2].lowercase()
        val value = parts[3]

        when (command) {
            "send" -> player.send(value)
            "player-command" -> player.player.performCommand(value)
            "console-command" -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value)
            "vault" -> {
                try {
                    val amount = value.toDouble()
                    VaultHook.give(player.player, amount)
                } catch (e: NumberFormatException) {
                    IEP.instance.logging.error("Invalid numerical value $value in rewards")
                }
            }
            else -> IEP.instance.logging.error("Invalid command $command in rewards")
        }
    }
}