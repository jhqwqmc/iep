package dev.efnilite.iep.generator

import dev.efnilite.iep.generator.util.PointType
import dev.efnilite.iep.generator.util.Section
import dev.efnilite.iep.world.World
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.util.Vector

private enum class Obstacle {

    PILLARS {
        override fun getPoints(center: Vector, radius: Int): List<Vector> {
            val points = mutableListOf<Vector>()
            val range = -radius..radius

            repeat(3) {
                val dz = range.random()

                for (dy in range) {
                    val new = center.clone().add(Vector(0, dy, dz))

                    if (new.distance(center) <= radius) {
                        points.add(new)
                    }
                }
            }

            return points
        }
    },
    LINES {
        override fun getPoints(center: Vector, radius: Int): List<Vector> {
            val points = mutableListOf<Vector>()
            val range = -radius..radius

            repeat(3) {
                val dy = range.random()

                for (dz in range) {
                    val new = center.clone().add(Vector(0, dy, dz))

                    if (new.distance(center) <= radius) {
                        points.add(new)
                    }
                }
            }

            return points
        }
    },
    HOLE_IN_WALL {
        override fun getPoints(center: Vector, radius: Int): List<Vector> {
            // todo
            return emptyList()
        }
    },
    CIRCLE {
        override fun getPoints(center: Vector, radius: Int): List<Vector> {
            return PointType.CIRCLE.getPoints(center, radius - 2)
        }
    },
    DIAMOND {
        override fun getPoints(center: Vector, radius: Int): List<Vector> {
            val points = mutableListOf<Vector>()
            val newCenter = center.clone().add(Vector(0, -radius + 2, 0))
            val r = radius - 2
            val range = 0..2 * r

            for (dy in range) {
                if (dy < r) {
                    points.add(newCenter.clone().add(Vector(0, dy, -dy)))
                    points.add(newCenter.clone().add(Vector(0, dy, dy)))
                    continue
                }

                val newDz = 2 * r - dy
                points.add(newCenter.clone().add(Vector(0, dy, -newDz)))
                points.add(newCenter.clone().add(Vector(0, dy, newDz)))
            }

            return points
        }
    };

    abstract fun getPoints(center: Vector, radius: Int): List<Vector>

}

class ObstacleGenerator : Generator() {

    private val obstacles = mutableMapOf<Int, List<Block>>()

    override fun generate() {
        super.generate()

        val latest = sections.maxBy { it.key }
        val idx = latest.key
        val section = latest.value

        val obstacle = Obstacle.entries.random()
        val points = obstacle.getPoints(section.end, settings.radius)

        println()
        println("obstacle: $obstacle")

        val blocks = mutableListOf<Block>()
        for (point in points) {
            val block = point.toLocation(World.world).block

            block.type = Material.YELLOW_CONCRETE

            blocks.add(block)
        }

        println("blocks: ${blocks.size}")

        obstacles[idx] = blocks
    }

    override fun clear(idx: Int, section: Section) {
        super.clear(idx, section)

        obstacles[idx]?.forEach { it.type = Material.AIR }

        obstacles.remove(idx)
    }
}