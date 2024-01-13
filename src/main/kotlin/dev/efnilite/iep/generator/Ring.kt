package dev.efnilite.iep.generator

import org.bukkit.util.Vector
import org.jetbrains.annotations.Contract
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

data class Ring(val heading: Vector, val center: Vector, val radius: Int) {

    init {
        assert(radius >= 0)
        assert(heading.isNormalized)
    }

    val blocks = getPositions()

    /**
     * Returns a list of vectors in a circle around [center] with radius [radius].
     * @return A list of vectors in a circle.
     */
    @Contract(pure = true)
    fun getPositions(): List<Vector> {
        if (radius == 0) {
            return emptyList()
        }

        val blocks = mutableListOf<Vector>()
        val centerX = center.x + 0.5
        val centerY = center.y + 0.5
        val centerZ = center.z + 0.5

        val accuracy = 60
        var t = 0.0
        repeat(accuracy) {
            t += 2 * Math.PI / accuracy

            val y = (centerY + radius * sin(t))
            val z = (centerZ + radius * cos(t))

            blocks.add(Vector(centerX, y, z))
        }

        return blocks
    }

    /**
     * Returns whether the given vector is near the ring's center.
     * @param vector The vector to check.
     * @return Whether the given vector is near the ring's center.
     */
    @Contract(pure=true)
    fun isNear(vector: Vector): Boolean {
        val dx = abs(center.x - vector.x)
        val dy = abs(center.y - vector.y)

        return dy <= radius - 1 && dx <= 2
    }
}