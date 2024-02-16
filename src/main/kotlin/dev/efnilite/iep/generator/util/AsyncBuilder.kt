package dev.efnilite.iep.generator.util

import dev.efnilite.iep.IEP
import dev.efnilite.vilib.util.Task
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*

/**
 * Builds a map of blocks at a specific rate.
 * This avoids lag spikes resulting in setting all blocks at once.
 * @param map The map of vectors to materials.
 */
class AsyncBuilder(
    map: () -> Map<Block, Material>
) {

    private var cancelled = false
    private lateinit var task: BukkitTask
    private val queue = LinkedList<Map.Entry<Block, Material>>()
    val blocks = mutableListOf<Block>()

    init {
        Task.create(IEP.instance)
            .async()
            .execute {
                queue.addAll(map.invoke().entries)

                initTask()
            }
            .run()
    }

    private fun initTask() {
        if (cancelled) return

        task = Task.create(IEP.instance)
            .repeat(1)
            .execute(
                object : BukkitRunnable() {
                    override fun run() {
                        repeat(BLOCKS_PER_TICK) {
                            if (cancelled || queue.isEmpty()) {
                                cancel()

                                return@run
                            }

                            val (block, material) = queue.poll()

                            block.type = material

                            blocks.add(block)
                        }
                    }
                })
            .run()
    }

    fun cancel() {
        cancelled = true
    }

    companion object {
        private const val BLOCKS_PER_TICK = 50
    }
}