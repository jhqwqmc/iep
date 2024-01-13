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
    fun next(): Vector {
        val dx = next(40, 5)
        val dy = next(0, 5)
        val dz = next(0, 10)

        return Vector(dx, dy, dz)
    }

    /**
     * Returns a random normally distributed value.
     */
    @Contract(pure = true)
    private fun next(mean: Int, sd: Int): Int {
        val distribution = ((mean - 2 * sd)..(mean + 2 * sd))
            .associateWith { Probs.normalpdf(mean.toDouble(), sd.toDouble(), it.toDouble()) }

        return Probs.random(distribution, random)
    }
}