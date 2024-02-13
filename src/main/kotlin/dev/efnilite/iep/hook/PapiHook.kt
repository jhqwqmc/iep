package dev.efnilite.iep.hook

import dev.efnilite.iep.ElytraPlayer.Companion.asElytraPlayer
import dev.efnilite.iep.IEP
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

object PapiHook : PlaceholderExpansion() {

    override fun getIdentifier() = "iep"
    override fun getAuthor() = "Efnilite"
    override fun getVersion() = IEP.instance.description.version
    override fun canRegister() = true
    override fun persist() = true

    override fun onPlaceholderRequest(player: Player, params: String): String? {
        // iep_default_score_1

        if (params.matches(Regex("iep_[a-z]+_[a-z]+_\\d+"))) {
            return request(params)
        }

        val iep = player.asElytraPlayer() ?: return null
        val generator = iep.getGenerator()

        when (params) {
            "score" -> generator.getScore()
            "time" -> generator.getTime()
            "seed" -> generator.settings.seed
            "speed" -> generator.getSpeed(iep)
        }

        return null
    }

    private fun request(params: String): String? {
        val parts = params.split("_")
        val mode = parts[1].lowercase()
        val type = parts[2].lowercase()
        val rank = parts[3].toInt()

        val leaderboard = IEP.getMode(mode)?.leaderboard ?: return null
        val score = leaderboard.getRank(rank)

        return when (type) {
            "name" -> score.name
            "score" -> score.score.toString()
            "time" -> score.getFormattedTime()
            "seed" -> score.seed.toString()
            else -> null
        }
    }
}