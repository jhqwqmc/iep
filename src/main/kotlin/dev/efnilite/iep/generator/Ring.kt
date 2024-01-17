package dev.efnilite.iep.generator

import org.bukkit.util.Vector
import org.jetbrains.annotations.Contract
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
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
        val centerX = floor(center.x) + 0.5
        val centerY = floor(center.y) + 0.5
        val centerZ = floor(center.z) + 0.5

        val accuracy = 2 * Math.PI / 60

        var t = 0.0
        while (t <= 2 * Math.PI) {
            val y = (centerY + radius * sin(t))
            val z = (centerZ + radius * cos(t))

            blocks.add(Vector(centerX, y, z))

            t += accuracy
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
        val dz = abs(center.z - vector.z)
        val r = radius - 1

        return dy * dy + dz * dz <= r * r && dx <= 1.5
    }

    /**
     * Returns whether the given vector is out of bounds.
     * @param vector The vector to check.
     * @return Whether the given vector is out of bounds.
     */
    @Contract(pure=true)
    fun isOutOfBounds(vector: Vector): Boolean {
        val dy = abs(center.y - vector.y)
        val dz = abs(center.z - vector.z)

        return dz > 100 || dy > 100
    }
}