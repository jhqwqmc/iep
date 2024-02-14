package dev.efnilite.iep.leaderboard

import dev.efnilite.iep.IEP
import dev.efnilite.vilib.util.Task
import java.io.File
import java.util.*

/**
 * Represents a mode's leaderboard
 */
data class Leaderboard(val name: String) {

    private val data = mutableMapOf<UUID, Score>()
    private val file = File(IEP.instance.dataFolder, "leaderboards/$name.json")

    init {
        file.parentFile.mkdirs()
        file.createNewFile()

        read()
    }

    /**
     * Asynchronously reads the leaderboard.
     */
    private fun read() {
        Task.create(IEP.instance)
            .async()
            .execute {
                val map = file.reader().use { IEP.GSON.fromJson(it, Map::class.java) ?: return@execute }

                for ((uuid, score) in map) {
                    data[UUID.fromString(uuid.toString())] = IEP.GSON.fromJson(score.toString(), Score::class.java)
                }
            }
            .run()
    }

    /**
     * Asynchronously saves the leaderboard.
     */
    fun write() {
        Task.create(IEP.instance)
            .async()
            .execute { file.writer().use { IEP.GSON.toJson(data, it) } }
            .run()
    }

    /**
     * Updates the leaderboard. If this player has a score with a higher score and lower time, it will not be updated.
     * @param uuid The player's UUID
     * @param score The player's score
     */
    fun update(uuid: UUID, score: Score) {
        val existing = data.getOrDefault(uuid, EMPTY_SCORE)

        if (existing.score > score.score) {
            return
        }
        if (existing.score == score.score && existing.time < score.time) {
            return
        }

        data[uuid] = score
    }

    /**
     * Returns the score instance of the specified player.
     * @param uuid The player's UUID
     * @return The score instance of this player, else an empty score
     */
    fun getScore(uuid: UUID): Score {
        return data.getOrDefault(uuid, EMPTY_SCORE)
    }

    /**
     * Returns the score instance at the specified rank.
     * @param rank The rank
     * @return The score instance at this rank, else an empty score
     */
    fun getRank(rank: Int): Score {
        val scores = data.values.sortedByDescending { it.score }

        if (rank >= scores.size) return EMPTY_SCORE

        return scores[rank]
    }

    fun getAllScores() = data.entries.sortedByDescending { it.value.score }

    companion object {
        private val EMPTY_SCORE = Score("?", 0.0, 0, 0)
    }
}