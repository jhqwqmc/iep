package dev.efnilite.iep.generator

import dev.efnilite.iep.Config
import dev.efnilite.iep.generator.util.PointType
import dev.efnilite.iep.leaderboard.Leaderboard
import org.bukkit.util.Vector

class TimeTrialGenerator : Generator() {

    override fun tick() {
        super.tick()

        val player = players[0]

        if (score >= SCORE) {
            reset(s = SEED)
            return
        }

        player.sendActionBar("${getProgressBar(score.toDouble(), SCORE.toDouble(), ACTIONBAR_LENGTH)} <reset><dark_gray>| <gray>$score/$SCORE")
    }

    override fun start(ld: Leaderboard, start: Vector, type: PointType) {
        super.start(ld, start, type)

        seed = SEED
    }

    companion object {
        val SEED = Config.CONFIG.getInt("mode-settings.time-trial.seed") { it >= 0 }
        val SCORE = Config.CONFIG.getInt("mode-settings.time-trial.score") { it > 0 }
        val ACTIONBAR_LENGTH = Config.CONFIG.getInt("mode-settings.time-trial.actionbar-length") { it > 0 }
    }
}