package dev.efnilite.iep.generator

class SpeedDemonGenerator : Generator() {

    private var maxSpeedSoFar = 0.0

    override fun getScore() = maxSpeedSoFar

    override fun tick() {
        super.tick()

        if (shouldScore()) {
            val speed = getSpeed(players[0])

            maxSpeedSoFar = maxOf(maxSpeedSoFar, speed)
        }
    }

    override fun reset(regenerate: Boolean, s: Int, overrideSeedSettings: Boolean) {
        super.reset(regenerate, s, overrideSeedSettings)

        maxSpeedSoFar = 0.0
    }
}