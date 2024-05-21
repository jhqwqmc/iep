package dev.efnilite.iep.reward

import dev.efnilite.iep.mode.Mode
import org.bukkit.entity.Player

interface RewardExecutor {

    fun addReward(player: Player, mode: Mode, reward: Reward)

    fun send(player: Player, message: String)

    fun playerCommand(player: Player, command: String)

    fun consoleCommand(command: String)

    fun give(player: Player, amount: Double)

}