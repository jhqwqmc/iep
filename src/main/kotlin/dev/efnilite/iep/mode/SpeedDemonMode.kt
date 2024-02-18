package dev.efnilite.iep.mode

import dev.efnilite.iep.config.Config
import dev.efnilite.iep.generator.SpeedDemonGenerator
import dev.efnilite.iep.leaderboard.Leaderboard

object SpeedDemonMode : Mode {

    override val name = "speed demon"

    override val leaderboard = Leaderboard(name)

    override fun getGenerator() = SpeedDemonGenerator(this)

    override fun formatDisplayScore(score: Double): String {
        return if (Config.CONFIG.getString("metric") == "metric") {
            "%.1f km/h".format(score * 3.6)
        } else {
            "%.1f mph".format(score * 2.23694)
        }
    }
}