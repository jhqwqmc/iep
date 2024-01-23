package dev.efnilite.iep.generator

import org.bukkit.util.Vector
import org.jetbrains.annotations.Contract
import kotlin.math.abs
import kotlin.math.floor

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

        var y = radius
        var z = 0
        var d = 3 - 2 * radius

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

        return dy * dy + dz * dz <= r * r && dx <= 2
    }

    /**
     * Returns whether the given vector is out of bounds.
     * @param vector The vector to check.
     * @return Whether the given vector is out of bounds.
     */
    @Contract(pure=true)
    fun isOutOfBounds(vector: Vector): Boolean {
        val dx = center.x - vector.x
        val dy = abs(center.y - vector.y)
        val dz = abs(center.z - vector.z)

        return dx < -1 || dz > 100 || dy > 100
    }
}