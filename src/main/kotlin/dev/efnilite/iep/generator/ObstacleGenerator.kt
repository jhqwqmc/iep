package dev.efnilite.iep.generator

import dev.efnilite.iep.generator.section.PointType
import dev.efnilite.iep.generator.section.Section
import dev.efnilite.iep.world.World
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.util.Vector
import java.util.concurrent.CompletableFuture

private enum class Obstacle {

    PILLARS {
        override fun getPoints(center: Vector, radius: Int): List<Vector> {
            val points = mutableListOf<Vector>()
            val range = -radius..radius

            for (dz in range.filter { it % 3 == 0 }) {
                for (dy in range) {
                    val new = center.clone().add(Vector(0, dy, dz))

                    if (new.distance(center) <= radius) {
                        points += new
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

            for (dy in range.filter { it % 3 == 0 }) {
                for (dz in range) {
                    val new = center.clone().add(Vector(0, dy, dz))

                    if (new.distance(center) <= radius) {
                        points += new
                    }
                }
            }

            return points
        }
    },
    HOLE_IN_WALL {
        override fun getPoints(center: Vector, radius: Int): List<Vector> {
            val random = { (-2..2).random() }
            val offset = center.clone().add(Vector(0, random(), random()))

            val radius2 = radius * radius
            val points = mutableListOf<Vector>()
            for (dy in -radius + 1..radius) {
                for (dz in -radius + 1..radius) {
                    val point = center.clone().add(Vector(0, dy, dz))

                    if (point.distanceSquared(center) <= radius2 &&
                        point.distanceSquared(offset) >= 2) {
                        points += point
                    }
                }
            }

            return points
        }
    },
    CIRCLE {
        fun get(center: Vector, radius: Int) = PointType.CIRCLE.getPoints(center, radius)

        override fun getPoints(center: Vector, radius: Int): List<Vector> {
            return (0..radius).filter { it > 0 && it % 2 == 0 }.flatMap { get(center, it) }
        }
    },
    DIAMOND {
        fun get(center: Vector, radius: Int): List<Vector> {
            val points = mutableListOf<Vector>()
            val newCenter = center.clone().add(Vector(0, -radius + 2, 0))
            val r = radius - 2
            val range = 0..2 * r

            for (dy in range) {
                if (dy < r) {
                    points += newCenter.clone().add(Vector(0, dy, -dy))
                    points += newCenter.clone().add(Vector(0, dy, dy))
                    continue
                }

                val newDz = 2 * r - dy
                points += newCenter.clone().add(Vector(0, dy, -newDz))
                points += newCenter.clone().add(Vector(0, dy, newDz))
            }

            return points
        }

        override fun getPoints(center: Vector, radius: Int): List<Vector> {
            return (0..radius).filter { it % 3 == 2 }.flatMap { get(center, it) }
        }
    };

    abstract fun getPoints(center: Vector, radius: Int): List<Vector>

}

class ObstacleGenerator : Generator() {

    private val obstacles = mutableMapOf<Int, MutableList<Block>>()

    override fun generate(waitForDisplay: CompletableFuture<Void>) {
        super.generate(waitForDisplay)

        val (idx, section) = sections.maxBy { it.key }

        generateObstacle(idx, section, 1)
        generateObstacle(idx, section, 3)
    }

    private fun generateObstacle(idx: Int, section: Section, knotIdx: Int) {
        val obstacle = Obstacle.entries.random()
        val points = obstacle.getPoints(section.getKnot(knotIdx), settings.radius)

        val blocks = mutableListOf<Block>()
        for (point in points) {
            val block = point.toLocation(World.world).block

            block.type = Material.YELLOW_CONCRETE

            blocks += block
        }

        obstacles.getOrDefault(idx, mutableListOf()).let {
            it += blocks
            obstacles[idx] = it
        }
    }

    override fun clear(idx: Int, section: Section) {
        super.clear(idx, section)

        obstacles[idx]?.forEach { it.type = Material.AIR }

        obstacles.remove(idx)
    }
}