package dev.efnilite.iep.generator.section

import dev.efnilite.iep.IEP
import dev.efnilite.iep.generator.Settings
import dev.efnilite.iep.generator.Settings.Companion.asStyle
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import kotlin.math.min

/**
 * Class for changing blocks on the client side.
 * Gradually sends block changes to ensure packets are sent when chunks are loaded. Avoids invisible blocks.
 */
class BlockChanger {

    private val toChange: MutableMap<Int, MutableSet<Block>> = mutableMapOf()

    fun set(player: Player, settings: Settings) {
        val performance = settings.performance
        val style = settings.style.asStyle()

        val chunk = player.location.chunk
        val lead = if (performance) PERFORMANCE_LEAD else min(4, player.clientViewDistance)
        val xs = (chunk.x..chunk.x + lead)

        for (x in xs) {
            val blocks = toChange[x] ?: continue

            IEP.log("Setting blocks at $x")

            if (performance) {
                blocks.forEach { it.type = style.next() }
            } else {
                player.sendBlockChanges(blocks.map {
                    val state = it.state

                    state.type = style.next()

                    return@map state
                })
            }

            toChange.remove(x)
        }
    }

    fun queue(new: MutableMap<Int, MutableSet<Block>>, settings: Settings) {
        val performance = settings.performance

        IEP.log("Queued chunks ${new.keys.toTypedArray().contentToString()}")

        new.forEach { (x, blocks) ->
            val selectedBlocks = if (performance) {
                (0..<PERFORMANCE_BLOCKS_PER_RING).map { blocks.random() }
            } else {
                blocks
            }

            toChange.getOrPut(x) { mutableSetOf() }.addAll(selectedBlocks)
        }
    }

    fun reset(player: Player, blocks: MutableMap<Int, MutableSet<Block>>, settings: Settings) {
        val performance = settings.performance

        blocks.values.forEach { blocksInChunks ->
            if (performance) {
                blocksInChunks.forEach { it.type = Material.AIR }
            } else {
                player.sendBlockChanges(blocksInChunks.map {
                    val state = it.state
                    state.type = Material.AIR
                    return@map state
                })
            }
        }
    }

    fun clear() {
        toChange.clear()
    }

    companion object {
        private const val PERFORMANCE_LEAD = 12
        private const val PERFORMANCE_BLOCKS_PER_RING = 32
    }
}