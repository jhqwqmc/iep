package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.TimeTrialGenerator
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.iep.menu.LeaderboardMenu

object TimeTrialMode : Mode {

    override val name = "time trial"

    override val leaderboard = Leaderboard(name).load()

    override val sort
        get() = LeaderboardMenu.Sort.TIME

    override fun getGenerator() = TimeTrialGenerator()
}