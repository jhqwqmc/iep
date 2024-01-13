package dev.efnilite.iep

import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.world.Divider
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.inventory.item.Item
import fr.mrmicky.fastboard.adventure.FastBoard
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.util.Vector

/**
 * Class for wrapping players.
 */
class ElytraPlayer(private val player: Player) {

    /**
     * The player's position.
     */
    val position
        get() = player.location.toVector()

    /**
     * The player's previous position.
     */
    val previousPosition = player.location.toVector()

    private val board = FastBoard(player)

    fun getGenerator(): Generator {
        return Divider.generators.first { it.players.contains(this) }
    }

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
                .color(TextColor.color(0x505050)))
    }

    fun setup() {
        player.clearActivePotionEffects()
        player.gameMode = GameMode.ADVENTURE
        player.isInvulnerable = true

        player.inventory.clear()

        player.inventory.chestplate = Item(Material.ELYTRA, "").unbreakable().build()
        player.inventory.addItem(Item(Material.FIREWORK_ROCKET, 64, "").build())
    }

    companion object {

        fun Player.asElytraPlayer(): ElytraPlayer? {
            return Divider.generators.flatMap { it.players }.firstOrNull { it.player == this }
        }
    }
}