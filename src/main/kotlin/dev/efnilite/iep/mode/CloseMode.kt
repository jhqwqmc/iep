package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.CloseGenerator
import dev.efnilite.iep.generator.util.PointType
import dev.efnilite.iep.leaderboard.Leaderboard

object CloseMode : Mode {

    override val name = "close"

    override val leaderboard = Leaderboard(name)

    override val pointType = PointType.FLAT

    override fun getGenerator() = CloseGenerator()

}