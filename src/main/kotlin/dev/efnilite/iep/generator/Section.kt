package dev.efnilite.iep.generator

import dev.efnilite.iep.generator.Section.Companion.KNOTS
import dev.efnilite.iep.style.Style
import dev.efnilite.iep.world.World
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator
import org.bukkit.util.Vector
import java.util.*

/**
 * Represents a section of the total parkour of size [KNOTS].
 *
 * @param start The start position of the section.
 * @param random The random instance to use.
 */
class Section(private val start: Vector, random: Random) {

    private val director = KnotDirector(random)
    private val interpolator = SplineInterpolator()
    private val knots = generateKnots()
    val points = generatePoints()

    /**
     * The end position of the section.
     */
    val end = points.last()

    /**
     * Returns whether the given vector is near the section.
     */
    fun isNear(vector: Vector) = knots.dropLast(2).last().distance(vector) < 10

    /**
     * Generates the section's points.
     */
    fun generate(style: Style) {
        val world = World.world

        AsyncSectionBuilder(points, style, world).run()
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

    private fun generateKnots(): List<Vector> {
        val knots = mutableListOf(start)

        repeat(KNOTS - 1) { knots.add(knots.last().clone().add(director.nextOffset())) }

        return knots
    }

    companion object {
        const val RADIUS = 5
        const val KNOTS = 5
        const val EXTRA_POINTS_OFFSET = 1
    }
}