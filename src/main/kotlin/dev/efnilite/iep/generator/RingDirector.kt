package dev.efnilite.iep.generator

import dev.efnilite.vilib.util.Probs
import org.bukkit.util.Vector
import org.jetbrains.annotations.Contract
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class RingDirector(private val random: Random = ThreadLocalRandom.current()) {

    /**
     * Returns a random vector.
     */
    @Contract(pure = true)
    fun nextOffset(): Vector {
        val dx = nextOffset(30, 5)
        val dy = nextOffset(-1, 3)
        val dz = nextOffset(0, 5)

        return Vector(dx, dy, dz)
    }

    /**
     * Returns a random radius.
     */
    @Contract(pure = true)
    fun nextRadius() = 4

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