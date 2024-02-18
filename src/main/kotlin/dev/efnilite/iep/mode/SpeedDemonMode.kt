package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.SpeedDemonGenerator
import dev.efnilite.iep.leaderboard.Leaderboard

object SpeedDemonMode : Mode {

    override val name = "speed demon"

    override val leaderboard = Leaderboard(name)

    override fun getGenerator() = SpeedDemonGenerator(this)
}