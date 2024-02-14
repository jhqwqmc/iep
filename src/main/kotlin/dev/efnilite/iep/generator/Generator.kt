package dev.efnilite.iep.generator

import dev.efnilite.iep.Config
import dev.efnilite.iep.ElytraPlayer
import dev.efnilite.iep.ElytraPlayer.Companion.asElytraPlayer
import dev.efnilite.iep.IEP
import dev.efnilite.iep.generator.util.Island
import dev.efnilite.iep.generator.util.PointType
import dev.efnilite.iep.generator.util.Section
import dev.efnilite.iep.generator.util.Settings
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.iep.leaderboard.Score
import dev.efnilite.iep.world.Divider
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.schematic.Schematics
import dev.efnilite.vilib.util.Task
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max

open class Generator {

    lateinit var settings: Settings
        private set
    val players = mutableListOf<ElytraPlayer>()

    protected lateinit var island: Island
    protected val sections = mutableMapOf<Int, Section>()
    protected var seed: Int = ThreadLocalRandom.current().nextInt(0, SEED_BOUND)

    private lateinit var task: BukkitTask
    private lateinit var leaderboard: Leaderboard
    private var start: Instant? = null
    private var pointType: PointType = PointType.CIRCLE
    private var random = Random()

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

            reset(false)

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

        reset()

        task = Task.create(IEP.instance)
            .repeat(1)
            .execute(::tick)
            .run()
    }

    /**
     * Updates all players' scoreboards.
     */
    private fun updateBoard(score: Double, time: Instant) {
        val formattedTime = DateTimeFormatter.ofPattern(Config.CONFIG.getString("time-format"))
            .withZone(ZoneOffset.UTC)
            .format(time)

        players.forEach { it.updateBoard(score, formattedTime, seed) }
    }

    open fun getScore() = max(0.0, (players[0].position.x - island.blockSpawn.x))

    fun getTime() = Instant.now().minusMillis(start?.toEpochMilli() ?: Instant.now().toEpochMilli())

    private var resetUp = false

    /**
     * Ticks the generator.
     */
    protected open fun tick() {
        val (idx, section) = sections.maxBy { it.key }

        val player = players[0]
        val pos = player.position
        val score = getScore()

        updateBoard(score, getTime())
        updateInfo()

        if (start == null && score > 0) {
            start = Instant.now()
        }

        if (sections.size > 2) {
            val (minIdx, minSection) = sections.minBy { it.key }

            clear(minIdx, minSection)
        }

        if (shouldReset(player, pos)) {
            reset()
            return
        }

        if (section.isNearKnot(pos, 0) && section.end.y < 25) {
            //(island.blockSpawn.x - section.beginning.x).toInt()
            val cloned = section.clone(Vector(0, 200, 0))

            sections[idx + 1] = cloned

            IEP.log("Generating cloned section for low y at ${idx + 1}")

            cloned.generate(settings, pointType)

            resetUp = true

            return
        }

        if (resetUp) {
            val previous = sections[idx - 1]!!

            if (!previous.isNearKnot(pos, 2)) {
                return
            }

            resetPlayerHeight()

            return
        }

        if (section.isNearKnot(pos, 2)) {
            generate()

            return
        }
    }

    private fun shouldReset(player: ElytraPlayer, pos: Vector): Boolean {
        val (idx, section) = sections
            .filter { pos.x < it.value.end.x }
            .minBy { it.key }

        val progress = pos.x - section.beginning.x

        if (progress < 0) {
            return false
        }

        val isPastSpawn = if (idx == 0) progress > 5 else true
        val isNotGliding = isPastSpawn && !player.player.isGliding
        val isOutOfBounds = isPastSpawn && !section.isNearPoint(pos, progress.toInt(), settings.radius.toDouble())

        if (isNotGliding) {
            IEP.log("Player ${player.name} is not gliding")
        } else if (isOutOfBounds) {
            IEP.log("Player ${player.name} is out of bounds")
        }

        return isNotGliding || isOutOfBounds
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

            return
        }

        val latest = sections.maxBy { it.key }
        val idx = latest.key
        val previous = latest.value
        val end = previous.end

        val section = Section(end, random)

        sections[idx + 1] = section

        IEP.log("Generating section at ${idx + 1}")

        section.generate(settings, pointType)
    }

    protected open fun clear(idx: Int, section: Section) {
        IEP.log("Clearing section at $idx")

        section.clear()

        sections.remove(idx)
    }

    /**
     * Resets the players and knots.
     */
    protected open fun reset(regenerate: Boolean = true, s: Int = ThreadLocalRandom.current().nextInt(0, SEED_BOUND)) {
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

            it.send("<dark_gray>===============")
            it.send("<gray>Score <white>${"%.1f".format(score.score)}")
            it.send("<gray>Time <white>${score.getFormattedTime()}")
            it.send("<gray>Seed <white>${score.seed}")
            it.send("<dark_gray>===============")
        }

        seed = s
        random = Random(seed.toLong())
        start = null
        resetUp = false

        sections.toMap().forEach { clear(it.key, it.value) }

        sections.clear()

        val spawn = island.playerSpawn.toLocation(World.world)
        spawn.yaw = -90f

        players.forEach { it.teleport(spawn) }

        if (!regenerate) return

        generate()
    }

    protected open fun resetPlayerHeight() {
        val player = players[0]
        val velocity = player.player.velocity

        IEP.log("Resetting player height, velocity = $velocity")

//        val dx = island.blockSpawn.x - sections[sections.keys.max() - 1]!!.beginning.x
        val to = player.player.location.add(0.0, 200.0, 0.0)

//        to.chunk.load()

        val (minIdx, minSection) = sections.minBy { it.key }

        clear(minIdx, minSection)

        player.player.teleportAsync(to)

        Task.create(IEP.instance)
            .delay(2)
            .execute {
                IEP.log("Restoring pre-teleport velocity, velocity = $velocity")
                player.player.velocity = velocity }
            .run()

        resetUp = false
    }

    private fun updateInfo() {
        if (!settings.info) return

        players.forEach { it.sendActionBar(getFormattedSpeed(it)) }
    }

    /**
     * Returns the current speed of the player in m/s.
     * @param player The player to get the speed for.
     * @return The current speed.
     */
    fun getSpeed(player: ElytraPlayer) = player.player.velocity.clone().setY(0).length() * 20

    /**
     * Returns the current formatted speed to one decimal.
     * @param player The player to get the speed for.
     * @param metric Whether to use metric (km/h) or imperial (mph).
     * @return The current formatted speed.
     */
    fun getFormattedSpeed(player: ElytraPlayer, metric: Boolean = true): String {
        val speedMeters = getSpeed(player)

        return if (metric) {
            "<gray>%.1f km/h".format(speedMeters * 3.6)
        } else {
            "<gray>%.1f mph".format(speedMeters * 2.236936)
        }
    }

    /**
     * Returns a formatted progress bar.
     * @param t The current value.
     * @param max The maximum value.
     * @param length The length of the progress bar.
     * @return The formatted progress bar.
     */
    fun getProgressBar(t: Double, max: Double, length: Int = 30): String {
        val increments = max / length.toDouble()

        return (0 until length)
            .map {
                if (it * increments < t) {
                    return@map "<green><bold>|"
                } else {
                    return@map "<reset><dark_gray>|"
                }
            }
            .joinToString("") { it }
    }

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

        /**
         * Creates a new generator.
         * @param player The player to create the generator for.
         */
        fun create(player: Player,
                   leaderboard: Leaderboard,
                   pointType: PointType = PointType.CIRCLE,
                   gen: () -> Generator) {
            IEP.log("Creating generator for ${player.name}, pointType = $pointType")

            remove(player)

            val elytraPlayer = ElytraPlayer(player)

            elytraPlayer.join()

            val generator = gen.invoke()

            Divider.add(generator)

            generator.add(elytraPlayer)

            generator.start(leaderboard, Divider.toLocation(generator), pointType)
        }

        /**
         * Removes a player from the generator.
         * @param player The player to remove.
         */
        fun remove(player: Player) {
            val elytraPlayer = player.asElytraPlayer() ?: return

            val generator = elytraPlayer.getGenerator()

            generator.remove(elytraPlayer)

            elytraPlayer.leave()
        }
    }
}