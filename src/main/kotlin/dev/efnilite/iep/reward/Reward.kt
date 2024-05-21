package dev.efnilite.iep.reward

import dev.efnilite.iep.IEP
import dev.efnilite.iep.hook.VaultHook
import dev.efnilite.iep.mode.Mode
import dev.efnilite.iep.player.ElytraPlayer.Companion.asElytraPlayer
import dev.efnilite.vilib.util.Strings
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * String representation of a reward.
 *
 * Format: <give-time>||<mode>||<command>||<value>
 */
data class Reward(val string: String, private val executor: RewardExecutor = RewardExecutorImpl) {

    /**
     * Executes the reward. If this is not a leave reward, will execute instantly.
     *
     * @param player The player to execute the reward on.
     * @param currentMode The mode the player is in.
     */
    fun execute(player: Player, currentMode: Mode) {
        val parts = string.split("||", limit = 4)

        val time = parts[0].lowercase()

        if (time == "leave") {
            executor.addReward(player, currentMode, Reward("now||${parts[1]}||${parts[2]}||${parts[3]}"))
            return
        }

        val modeName = parts[1].lowercase()

        if (modeName != "all") {
            val mode = IEP.getMode(modeName)

            if (mode == null) {
                IEP.logging.error("Invalid mode ${currentMode.name} in rewards")
                return
            }

            if (mode != currentMode) {
                return
            }
        }

        val command = parts[2].lowercase()
        val value = parts[3].replace("%player%", player.name)

        when (command) {
            "send" -> executor.send(player, value)
            "player command" -> executor.playerCommand(player, value)
            "console command" -> executor.consoleCommand(value)
            "vault" -> {
                try {
                    val amount = value.toDouble()
                    executor.give(player, amount)
                } catch (e: NumberFormatException) {
                    IEP.logging.error("Invalid numerical value $value in rewards")
                }
            }

            else -> IEP.logging.error("Invalid command $command in rewards")
        }
    }
}

private object RewardExecutorImpl : RewardExecutor {

    override fun addReward(player: Player, mode: Mode, reward: Reward) {
        player.asElytraPlayer()?.addReward(mode, reward)
    }

    override fun send(player: Player, message: String) {
        player.sendMessage(Strings.colour(message))
    }

    override fun playerCommand(player: Player, command: String) {
        player.performCommand(command)
    }

    override fun consoleCommand(command: String) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
    }

    override fun give(player: Player, amount: Double) {
        VaultHook.give(player, amount)
    }
}