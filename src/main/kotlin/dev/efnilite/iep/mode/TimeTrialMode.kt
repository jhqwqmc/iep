package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.TimeTrialGenerator
import dev.efnilite.iep.leaderboard.Leaderboard

object TimeTrialMode : Mode {

    override val name = "time trial"

    override val leaderboard = Leaderboard(name)

    override fun getGenerator() = TimeTrialGenerator()
}