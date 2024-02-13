package dev.efnilite.iep.generator

class SpeedDemonGenerator : Generator() {

    private var maxSpeedSoFar = 0.0

    override fun getScore() = maxSpeedSoFar

    override fun tick() {
        super.tick()

        val speed = getSpeed(players[0])

        maxSpeedSoFar = maxOf(maxSpeedSoFar, speed)
    }

    override fun reset(regenerate: Boolean, s: Int) {
        super.reset(regenerate, s)

        maxSpeedSoFar = 0.0
    }
}