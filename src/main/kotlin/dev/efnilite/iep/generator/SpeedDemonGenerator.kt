package dev.efnilite.iep.generator

class SpeedDemonGenerator : Generator() {

    private var maxSpeedSoFar = 0.0

    override val score: Int
        get() = maxSpeedSoFar.toInt() // todo add Double

    override fun tick() {
        super.tick()

        val speed = getSpeed(players[0])

        maxSpeedSoFar = maxOf(maxSpeedSoFar, speed)
    }

    override fun reset(regenerate: Boolean) {
        super.reset(regenerate)

        maxSpeedSoFar = 0.0
    }
}