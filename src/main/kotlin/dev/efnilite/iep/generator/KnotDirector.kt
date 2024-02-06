package dev.efnilite.iep.generator

import dev.efnilite.vilib.util.Probs
import org.bukkit.util.Vector
import org.jetbrains.annotations.Contract
import java.util.*

class KnotDirector(private val random: Random) {

    /**
     * Returns a random vector.
     */
    @Contract(pure = true)
    fun nextOffset(): Vector {
        val dx = nextOffset(75, 15)
        val dy = nextOffset(-15, 3)
        val dz = nextOffset(0, 35)

        return Vector(dx, dy, dz)
    }

    /**
     * Returns a random radius.
     */
    @Contract(pure = true)
    fun nextRadius() = 5

    /**
     * Returns a random normally distributed value.
     */
    @Contract(pure = true)
    private fun nextOffset(mean: Int, sd: Int): Int {
        val distribution = ((mean - 2 * sd)..(mean + 2 * sd))
            .associateWith { Probs.normalpdf(mean.toDouble(), sd.toDouble(), it.toDouble()) }

        return Probs.random(distribution, random)
    }
}