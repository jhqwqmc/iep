package dev.efnilite.iep.generator.section

import dev.efnilite.iep.IEP
import dev.efnilite.iep.generator.Settings
import dev.efnilite.iep.generator.section.Section.Companion.KNOTS
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.util.Task
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

/**
 * Represents a section of the total parkour of size [KNOTS].
 */
class Section {

    private val director: KnotDirector
    private val interpolator: SplineInterpolator
    private val knots: List<Vector>
    private val points: List<Vector>
    private val blocks = CompletableFuture<MutableMap<Int, MutableSet<Block>>>()

    // TODO yikes!
    private lateinit var builder: BukkitTask

    /**
     * The beginning position of the section.
     */
    val beginning
        get() = knots.first().clone()

    /**
     * The end position of the section.
     */
    val end
        get() = knots.last().clone()

    /**
     * Constructs a section with the given start position and random.
     * @param start The start position of the section.
     * @param random The random to use for the section.
     */
    constructor(start: Vector, random: Random) {
        director = KnotDirector(random)
        interpolator = SplineInterpolator()
        knots = generateKnots(start)
        points = generatePoints()
    }

    private constructor(section: Section, offset: Vector) {
        director = section.director
        interpolator = section.interpolator
        knots = section.knots.map { it.clone().add(offset) }
        points = section.points.map { it.clone().add(offset) }
    }

    fun getKnot(idx: Int) = knots[idx]
    fun getPoint(idx: Int) = points[idx]

    /**
     * Returns whether the given vector is near a section knot.
     */
    fun isNearKnot(vector: Vector, idx: Int, distance: Double = 6.0) =
        knots[idx].distanceSquared(vector) < distance * distance

    /**
     * Returns whether the given vector is near a section point.
     */
    fun isNearPoint(vector: Vector, idx: Int, distance: Double = 6.0) =
        points[idx].distanceSquared(vector) < distance * distance

    /**
     * Clones the section with the given offset.
     */
    fun clone(offset: Vector) = Section(this, offset)

    /**
     * Generates the section's points.
     */
    fun generate(
        settings: Settings, pointType: PointType
    ): CompletableFuture<MutableMap<Int, MutableSet<Block>>> {
        val world = World.world

        builder = Task.create(IEP.instance)
            .async()
            .execute {
                val currentBlocks: MutableMap<Int, MutableSet<Block>> = mutableMapOf()

                for (point in points) {
                    val byChunk = pointType.getPoints(point, settings.radius)
                        .map { it.toLocation(world).block }
                        .groupBy { it.chunk }

                    for ((chunk, b) in byChunk) {
                        currentBlocks.getOrPut(chunk.x) { mutableSetOf() }.addAll(b)
                    }
                }

                blocks.complete(currentBlocks)
            }.run()

        return blocks
    }

    fun clear(player: Player) {
        builder.cancel()

        blocks.thenApply { b ->
            b.values.forEach { blocks ->
                player.sendBlockChanges(blocks.map {
                    val state = it.state
                    state.type = Material.AIR
                    return@map state
                })
            }
        }
    }

    private fun generatePoints(): List<Vector> {
        val knots = knots.toMutableList()

        // add points to force spline to have an angle of 0 at the start and end
        knots.add(0, knots.first().clone().subtract(Vector(EXTRA_POINTS_OFFSET, 0, 0)))
        knots.add(knots.last().clone().add(Vector(EXTRA_POINTS_OFFSET, 0, 0)))

        val xs = knots.map { it.x }.toDoubleArray()
        val ys = knots.map { it.y }.toDoubleArray()
        val zs = knots.map { it.z }.toDoubleArray()

        val splineY = interpolator.interpolate(xs, ys)
        val splineZ = interpolator.interpolate(xs, zs)

        val points = mutableListOf<Vector>()

        val actualXFirst = xs.drop(EXTRA_POINTS_OFFSET).first().toInt()
        val actualXLast = xs.dropLast(EXTRA_POINTS_OFFSET).last().toInt()

        for (x in actualXFirst..actualXLast) {
            val y = splineY.value(x.toDouble())
            val z = splineZ.value(x.toDouble())

            points.add(Vector(x, y.toInt(), z.toInt()))
        }

        return points
    }

    private fun generateKnots(start: Vector): List<Vector> {
        val knots = mutableListOf(start)

        repeat(KNOTS - 1) { knots.add(knots.last().clone().add(director.nextOffset())) }

        return knots
    }

    companion object {
        private const val KNOTS = 5
        private const val EXTRA_POINTS_OFFSET = 1
    }
}