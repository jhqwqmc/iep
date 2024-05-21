package dev.efnilite.iep.mode

import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.leaderboard.Leaderboard

object DefaultMode : Mode {

    override val name = "default"

    override val leaderboard = Leaderboard(name).load()

    override fun getGenerator() = Generator()
}