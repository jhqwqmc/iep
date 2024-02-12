package dev.efnilite.iep.generator.util

import dev.efnilite.iep.generator.util.Section.Companion.KNOTS
import dev.efnilite.iep.world.World
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.util.Vector
import java.util.*

/**
 * Represents a section of the total parkour of size [KNOTS].
 */
class Section {

    // TODO yikes!
    private var builder: AsyncBuilder? = null

    private val director: KnotDirector
    private val interpolator: SplineInterpolator
    private val knots: List<Vector>
    private val points: List<Vector>

    /**
     * The beginning position of the section.
     */
    val beginning
        get() = knots.first()

    /**
     * The end position of the section.
     */
    val end
        get() = knots.last()

    /**
     * The blocks that make up the rings of the section.
     */
    val blocks = mutableListOf<Block>()

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

    /**
     * Returns whether the given vector is near a section knot.
     */
    fun isNearKnot(vector: Vector, idx: Int, distance: Double = 6.0) = knots[idx].distance(vector) < distance

    /**
     * Returns whether the given vector is near a section point.
     */
    fun isNearPoint(vector: Vector, idx: Int, distance: Double = 6.0) = points[idx].distance(vector) < distance

    /**
     * Clones the section with the given offset.
     */
    fun clone(offset: Vector) = Section(this, offset)

    /**
     * Generates the section's points.
     */
    fun generate(settings: Settings, pointType: PointType) {
        val world = World.world

        val map = points.flatMap { pointType.getPoints(it, settings.radius) }
            .map { it.toLocation(world).block }
            .associateWith { settings.style.next() }

        builder = AsyncBuilder(map) { blocks.add(it) }
    }

    fun clear() {
        builder?.cancel()

        AsyncBuilder(blocks.associateWith { Material.AIR })
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