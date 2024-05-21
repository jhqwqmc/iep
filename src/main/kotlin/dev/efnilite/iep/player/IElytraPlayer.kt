package dev.efnilite.iep.player

import dev.efnilite.iep.generator.Settings
import dev.efnilite.iep.mode.Mode
import dev.efnilite.iep.reward.Reward
import org.bukkit.util.Vector
import java.util.*

interface IElytraPlayer {

    val position
        get() = Vector(0, 0, 0)

    val name
        get() = "player"
    val uuid: UUID
        get() = UUID.randomUUID()

    /**
     * Joins a [Mode].
     */
    fun join(mode: Mode)

    /**
     * Leaves the current mode.
     */
    fun leave(switchMode: Boolean = false, urgent: Boolean = false)


    /**
     * Loads the player's [Settings].
     */
    fun load(): Settings

    /**
     * Saves the player's [Settings].
     * @param settings The settings to save.
     */
    fun save(settings: Settings)

    /**
     * Sends a message to the player.
     * @param message The message to send.
     */
    fun send(message: String)

    /**
     * Sends a message to the player's action bar.
     * @param message The message to send.
     */
    fun sendActionBar(message: String)

    /**
     * Adds a reward to the player.
     * @param mode The mode the reward is for.
     * @param reward The reward to add.
     */
    fun addReward(mode: Mode, reward: Reward)
}