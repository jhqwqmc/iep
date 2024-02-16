package dev.efnilite.iep.generator

import dev.efnilite.iep.config.Config
import kotlin.math.min

class TimeTrialGenerator : Generator() {

    override fun getScore() = min(SCORE, super.getScore())

    override fun tick() {
        super.tick()

        val score = getScore()
        val player = players[0]

        if (score >= SCORE) {
            reset()
            return
        }

        player.sendActionBar("${getProgressBar(score, SCORE, ACTIONBAR_LENGTH)} <reset><dark_gray>| " +
                "<gray>${"%.1f".format(score)}/$SCORE")
    }

    override fun reset(regenerate: Boolean, s: Int, overrideSeedSettings: Boolean) {
        super.reset(regenerate, SEED, true)
    }

    companion object {
        val SEED = Config.CONFIG.getInt("mode-settings.time-trial.seed") { it >= 0 }
        val SCORE = Config.CONFIG.getDouble("mode-settings.time-trial.score") { it > 0 }
        val ACTIONBAR_LENGTH = Config.CONFIG.getInt("mode-settings.time-trial.actionbar-length") { it > 0 }
    }
}