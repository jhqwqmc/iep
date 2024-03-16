package dev.efnilite.iep.generator.section

import dev.efnilite.iep.IEP
import dev.efnilite.iep.style.Style
import org.bukkit.block.Block
import org.bukkit.entity.Player

/**
 * Class for changing blocks on the client side.
 * Gradually sends block changes to ensure packets are sent when chunks are loaded. Avoids invisible blocks.
 */
class ClientBlockChanger {

    private val toChange: MutableMap<Int, MutableSet<Block>> = mutableMapOf()

    fun check(player: Player, style: Style) {
        val chunk = player.location.chunk
        val xs = (chunk.x..chunk.x + RENDER_DISTANCE)

        for (x in xs) {
            val blocks = toChange[x] ?: continue

            IEP.log("Displaying ${blocks.size} blocks at $x")

            player.sendBlockChanges(blocks.map {
                val state = it.state

                state.type = style.next()

                return@map state
            })

            toChange.remove(x)
        }
    }

    fun queue(new: MutableMap<Int, MutableSet<Block>>) {
        IEP.log("Queued chunks ${new.keys.toTypedArray().contentToString()}")

        new.forEach { (x, blocks) ->
            toChange.getOrPut(x) { mutableSetOf() }.addAll(blocks)
        }
    }

    fun clear() {
        toChange.clear()
    }

    companion object {
        private const val RENDER_DISTANCE = 5
    }

}