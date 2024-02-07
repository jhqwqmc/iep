package dev.efnilite.iep.generator

import dev.efnilite.iep.IEP
import dev.efnilite.vilib.util.Task
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

/**
 * Builds a map of blocks at a specific rate.
 * This avoids lag spikes resulting in setting all blocks at once.
 * @param map The map of vectors to materials.
 * @param onComplete The function to call when the building is complete.
 */
class AsyncBuilder(
    map: Map<Block, Material>,
    onComplete: (List<Block>) -> Unit = { }
) {

    private val queue = LinkedList(map.entries)
    private val blocks = mutableListOf<Block>()

    init {
        Task.create(IEP.instance)
            .repeat(1)
            .execute(
                object : BukkitRunnable() {
                    override fun run() {
                        repeat(BLOCKS_PER_TICK) {

                            if (queue.isEmpty()) {
                                cancel()
                                onComplete.invoke(blocks)

                                return@repeat
                            }

                            val (block, material) = queue.poll()

                            block.type = material

                            blocks.add(block)
                        }
                    }
                })
            .run()
    }

    companion object {
        private const val BLOCKS_PER_TICK = 100
    }
}