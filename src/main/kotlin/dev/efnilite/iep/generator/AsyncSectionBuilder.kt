package dev.efnilite.iep.generator

import dev.efnilite.iep.IEP
import dev.efnilite.iep.style.Style
import dev.efnilite.vilib.util.Task
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.floor

/**
 * Builds a section of the parkour at a specific rate.
 * This avoids lag spikes resulting in setting all blocks at once.
 */
class AsyncSectionBuilder(centers: List<Vector>, private val style: Style, private val world: World) {

    private val queue = LinkedList(centers)

    /**
     * Starts building.
     */
    fun run() {
        Task.create(IEP.instance)
            .repeat(1)
            .execute(object : BukkitRunnable() {
                override fun run() = repeat(2) { run(this) }
            }).run()
    }

    private fun run(task: BukkitRunnable) {
        val center = queue.poll()

        if (center == null) {
            // no blocks left, so cancel task
            task.cancel()

            return
        }

        for (vector in getCircle(center)) {
            vector.toLocation(world).block.type = style.next()
        }
    }

    private fun getCircle(center: Vector): List<Vector> {
        val blocks = mutableListOf<Vector>()
        val centerX = floor(center.x) + 0.5
        val centerY = floor(center.y) + 0.5
        val centerZ = floor(center.z) + 0.5

        var y = Section.RADIUS
        var z = 0
        var d = 3 - 2 * Section.RADIUS

        while (z <= y) {
            blocks.add(Vector(centerX, centerY + y, centerZ + z))
            blocks.add(Vector(centerX, centerY + y, centerZ - z))
            blocks.add(Vector(centerX, centerY - y, centerZ + z))
            blocks.add(Vector(centerX, centerY - y, centerZ - z))
            blocks.add(Vector(centerX, centerY + z, centerZ + y))
            blocks.add(Vector(centerX, centerY + z, centerZ - y))
            blocks.add(Vector(centerX, centerY - z, centerZ + y))
            blocks.add(Vector(centerX, centerY - z, centerZ - y))

            if (d < 0) {
                d += 4 * z + 6
            } else {
                d += 4 * (z - y) + 10
                y--
            }
            z++
        }

        return blocks
    }
}