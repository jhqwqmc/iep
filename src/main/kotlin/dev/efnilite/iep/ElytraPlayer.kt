package dev.efnilite.iep

import dev.efnilite.iep.world.World
import fr.mrmicky.fastboard.adventure.FastBoard
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.util.Vector

/**
 * Class for wrapping players.
 */
class ElytraPlayer(val player: Player) {

    private val board = FastBoard(player)

    /**
     * The player's location.
     */
    val location
        get() = player.location.toVector()

    fun teleport(vector: Vector) {
        player.teleportAsync(vector.toLocation(World.world))
    }

    fun updateBoard(score: Int, time: String) {
        board.updateTitle(Component.text("IEP")
            .color(TextColor.color(0x800000))
            .decorate(TextDecoration.BOLD))

        board.updateLines(Component.text(""),
            Component.text("Score $score"),
            Component.text("Time $time"),
            Component.text(""),
            Component.text("server.ip")
                .color(TextColor.color(0x707070)))
    }

}