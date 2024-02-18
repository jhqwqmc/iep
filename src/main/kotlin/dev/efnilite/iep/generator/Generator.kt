package dev.efnilite.iep.generator

import dev.efnilite.iep.IEP
import dev.efnilite.iep.config.Config
import dev.efnilite.iep.config.Locales
import dev.efnilite.iep.generator.section.PointType
import dev.efnilite.iep.generator.section.Section
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.iep.leaderboard.Score
import dev.efnilite.iep.mode.Mode
import dev.efnilite.iep.player.ElytraPlayer
import dev.efnilite.iep.reward.Rewards
import dev.efnilite.iep.world.Divider
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.schematic.Schematics
import dev.efnilite.vilib.util.Task
import org.bukkit.Chunk
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.random.Random

private class RewardHandler(val mode: Mode) {

    /**
     * Achieved score rewards this run.
     */
    private val scoreRewards = mutableListOf<Int>()

    /**
     * Achieved interval rewards this run.
     */
    private val intervalRewards = mutableListOf<Int>()

    /**
     * Achieved one-time rewards this run.
     */
    private val oneTimeRewards = mutableListOf<Int>()

    fun checkScores(score: Int, generator: Generator) {
        for (player in generator.players) {
            ((score - 4)..score)
                .filter { it > 0 }
                .forEach { check(it, player.player, generator) }
        }
    }

    private fun check(score: Int, player: Player, generator: Generator) {
        for ((key, rewards) in Rewards.scoreRewards) {
            if (score < key || key in scoreRewards) continue

            scoreRewards.add(key)

            rewards.forEach { it.execute(player, mode) }
        }

        for ((key, rewards) in Rewards.oneTimeRewards) {
            if (score < key || score in oneTimeRewards || key in generator.settings.rewards) continue

            oneTimeRewards.add(score)
            generator.set { settings ->
                val newRewards = settings.rewards
                newRewards.add(key)
                return@set Settings(settings, rewards = newRewards)
            }

            rewards.forEach { it.execute(player, mode) }
        }

        for ((key, rewards) in Rewards.intervalRewards) {
            if (score % key != 0 || score in intervalRewards) continue

            intervalRewards.add(score)

            rewards.forEach { it.execute(player, mode) }
        }
    }

    fun clear() {
        scoreRewards.clear()
        oneTimeRewards.clear()
    }
}

open class Generator(val mode: Mode) {

    private val rewardHandler = RewardHandler(mode)

    lateinit var settings: Settings
        private set
    val players = mutableListOf<ElytraPlayer>()

    private val chunks = mutableMapOf<String, Chunk>()
    protected val sections = mutableMapOf<Int, Section>()
    var seed = 0
        protected set

    private lateinit var island: Island
    private lateinit var task: BukkitTask
    private lateinit var leaderboard: Leaderboard
    private var start: Instant? = null
    private var pointType: PointType = PointType.CIRCLE
    private var random = Random(0)
    private var movementScore = 0.0
    private var resetTo: Vector? = null

    /**
     * Adds a player to the generator.
     * @param player The player to add.
     */
    fun add(player: ElytraPlayer) {
        IEP.log("Adding player to generator ${player.name}")

        players.add(player)

        settings = player.load()
    }

    /**
     * Removes a player from the generator.
     * @param player The player to remove.
     */
    fun remove(player: ElytraPlayer) {
        IEP.log("Removing player from generator ${player.name}")

        player.save(settings)

        players.remove(player)

        if (players.isEmpty()) {
            IEP.log("Players is empty, clearing generator")

            task.cancel()

            reset(ResetReason.RESET, false)

            island.clear()

            Divider.remove(this)
        }
    }

    /**
     * Initializes all the stuff.
     * @param start The vector to spawn the island at.
     */
    open fun start(ld: Leaderboard, start: Vector, point: PointType) {
        IEP.log("Starting generator at $start")

        leaderboard = ld
        island = Island(start, Schematics.getSchematic(IEP.instance, "spawn-island"))
        pointType = point

        reset(ResetReason.RESET)

        task = Task.create(IEP.instance)
            .repeat(1)
            .execute(::tick)
            .run()
    }

    fun shouldScore(): Boolean {
        val player = players[0]

        return player.player.isGliding && player.position.x - island.blockSpawn.x > 0
    }

    open fun getScore() = max(0.0, movementScore)

    fun getTime() = Instant.now().minusMillis(start?.toEpochMilli() ?: Instant.now().toEpochMilli())

    fun getHighScore() = leaderboard.getScore(players.first().uuid)

    /**
     * Ticks the generator.
     */
    protected open fun tick() {
        val (idx, section) = sections.maxBy { it.key }

        val player = players[0]
        val pos = player.position
        val score = getScore()

        if (shouldScore()) {
            this.movementScore += player.player.velocity.x
        }

        player.player.setPlayerTime(settings.time.toLong(), false)
        if (Rewards.enabled) {
            rewardHandler.checkScores(score.toInt(), this)
        }

        updateBoard(score, getTime())
        updateInfo()

        if (start == null && score > 0) {
            start = Instant.now()
        }

        if (sections.size > 2) {
            val (minIdx, minSection) = sections.minBy { it.key }

            clear(minIdx, minSection)
        }

        val resetReason = shouldReset(player, pos)
        if (resetReason != null) {
            reset(resetReason)
            return
        }

        if (section.isNearKnot(pos, 0) && section.end.y < 25) {
            val toStart = island.blockSpawn.clone().subtract(section.beginning)
            val cloned = section.clone(toStart)

            sections[idx + 1] = cloned

            IEP.log("Generating cloned section for low y at ${idx + 1}")

            cloned.generate(settings, pointType, 100)

            resetTo = toStart

            return
        }

        if (resetTo != null) {
            val previous = sections[idx - 1]!!

            if (!previous.isNearKnot(pos, 2)) {
                return
            }

            resetPlayerHeight(resetTo!!)

            resetTo = null

            return
        }

        if (section.isNearKnot(pos, 2)) {
            generate()

            return
        }
    }

    private fun updateBoard(score: Double, time: Instant) {
        val formattedTime = DateTimeFormatter.ofPattern(Config.CONFIG.getString("time-format"))
            .withZone(ZoneOffset.UTC)
            .format(time)

        players.forEach { it.updateBoard(score, formattedTime, seed) }
    }

    private fun updateInfo() {
        if (!settings.info) return

        players.forEach {
            val speed = getSpeed(it)

            it.sendActionBar("<gray>${convertSpeed(speed)}") }
    }

    private fun convertSpeed(speed: Double): String {
        return if (settings.metric) {
            "%.1f km/h".format(speed * 3.6)
        } else {
            "%.1f mph".format(speed * 2.236936)
        }
    }

    private fun shouldReset(player: ElytraPlayer, pos: Vector): ResetReason? {
        val (idx, section) = sections
            .filter { pos.x < it.value.end.x }
            .minBy { it.key }

        val progress = pos.x - section.beginning.x

        if (progress < 0) {
            if (idx == 0 && pos.y < island.blockSpawn.y - settings.radius) {
                IEP.log("Player ${player.name} is below spawn")

                return ResetReason.BOUNDS
            }

            return null
        }

        val isPastSpawn = progress > 0
        val isNotGliding = isPastSpawn && !player.player.isGliding
        val isOutOfBounds = isPastSpawn && !section.isNearPoint(pos, progress.toInt(), settings.radius.toDouble())

        if (isNotGliding) {
            IEP.log("Player ${player.name} is not gliding")
            return ResetReason.FLYING
        } else if (isOutOfBounds) {
            IEP.log("Player ${player.name} is out of bounds")
            return ResetReason.BOUNDS
        }

        return null
    }

    /**
     * Generates the next knot.
     */
    protected open fun generate() {
        if (sections.isEmpty()) {
            val section = Section(island.blockSpawn.clone().add(Vector(0, pointType.heightOffset, 0)), random)

            sections[0] = section

            IEP.log("Generating section at 0")

            section.generate(settings, pointType)

            section.awaitChunks().thenApply { chunks.putAll(it) }

            return
        }

        val latest = sections.maxBy { it.key }
        val idx = latest.key
        val previous = latest.value
        val end = previous.end

        val section = Section(end, random)

        section.awaitChunks().thenApply { chunks.putAll(it) }

        sections[idx + 1] = section

        IEP.log("Generating section at ${idx + 1}")

        section.generate(settings, pointType)
    }

    protected open fun resetPlayerHeight(toStart: Vector) {
        val player = players[0]
        val velocity = player.player.velocity

        IEP.log("Resetting player height, velocity = $velocity")

        val to = player.player.location.add(toStart)

        val (minIdx, minSection) = sections.minBy { it.key }

        clear(minIdx, minSection)

        player.player.teleportAsync(to)

        Task.create(IEP.instance)
            .delay(5)
            .execute {
                IEP.log("Restoring pre-teleport velocity, velocity = $velocity")
                player.player.velocity = velocity }
            .run()
    }

    protected open fun clear(idx: Int, section: Section) {
        IEP.log("Clearing section at $idx")

        section.clear()

        sections.remove(idx)
    }

    /**
     * Resets the players and knots.
     */
    open fun reset(resetReason: ResetReason, regenerate: Boolean = true, s: Int = 0, overrideSeedSettings: Boolean = false) {
        IEP.log("Resetting generator, regenerate = $regenerate, seed = $s")

        players.forEach {
            if (getScore() == 0.0) {
                return@forEach
            }

            val score = Score(
                name = it.name,
                score = getScore(),
                time = getTime().toEpochMilli(),
                seed = seed
            )

            leaderboard.update(it.uuid, score)

            Locales.getStringList(it, "reset.lines").map { line -> updateLine(line, score, resetReason) }
                .forEach { line -> it.send(line) }
        }

        movementScore = 0.0
        start = null
        resetTo = null
        if (settings.seed == -1 && !overrideSeedSettings) {
            seed = ThreadLocalRandom.current().nextInt(SEED_BOUND)
            random = Random(seed)
        } else {
            seed = s
            random = Random(s)
        }

        sections.toMap().forEach { clear(it.key, it.value) }

        sections.clear()

        rewardHandler.clear()

        val spawn = island.playerSpawn.toLocation(World.world)
        spawn.yaw = -90f

        players.forEach { it.teleport(spawn) }

        if (regenerate) {
            generate()
            return
        }

        chunks.values.forEach {
            it.removePluginChunkTicket(IEP.instance)
            it.unload()
        }

        chunks.clear()
    }

    private fun updateLine(line: String, score: Score, resetReason: ResetReason): String {
        return line.replace("%score%", "%.1f".format(score.score))
            .replace("%high-score%", "%.1f".format(getHighScore().score))
            .replace("%time%", score.getFormattedTime())
            .replace("%seed%", score.seed.toString())
            .replace("%reason%", Locales.getString(settings.locale, "reset.reasons.${resetReason.name.lowercase()}"))
    }

    /**
     * Returns the current speed of the player in m/s.
     * @param player The player to get the speed for.
     * @return The current speed.
     */
    fun getSpeed(player: ElytraPlayer) = player.player.velocity.clone().setY(0).length() * 20

    /**
     * Allows for easy setting of the current [Settings] instance.
     */
    fun set(mapper: (Settings) -> Settings) {
        IEP.log("Updating settings from $settings to ${mapper.invoke(settings)}")

        settings = mapper.invoke(settings)

        players.forEach { it.save(settings) }
    }

    companion object {

        const val SEED_BOUND = 1_000_000

    }
}