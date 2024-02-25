package dev.efnilite.iep.leaderboard

import dev.efnilite.iep.config.Config
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class Score(val name: String, val score: Double, val time: Long, val seed: Int) {

    /**
     * @return The time in a formatted manner.
     */
    fun getFormattedTime(): String {
        return DateTimeFormatter.ofPattern(Config.CONFIG.getString("time-format"))
            .withZone(ZoneOffset.UTC)
            .format(Instant.ofEpochMilli(time))
    }

    companion object {

        fun Double.pretty(digits: Int = 1) = "%.${digits}f".format(this)

    }
}