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

    protected val sections = mutableMapOf<Int, Section>()

    private var start: Instant? = null
    private var pointType: PointType = PointType.CIRCLE
    private lateinit var task: BukkitTask

    protected lateinit var island: Island

    protected var seed: Int = ThreadLocalRandom.current().nextInt(0, SEED_BOUND)
    private var random = Random()

    private lateinit var leaderboard: Leaderboard

    val players = mutableListOf<ElytraPlayer>()
    var settings: Settings = Settings(IEP.getStyles().random(), 5, seed, true)
        private set

    /**
     * Adds a player to the generator.
     * @param player The player to add.
     */
    fun add(player: ElytraPlayer) {
        players.add(player)
    }

    /**
     * Removes a player from the generator.
     * @param player The player to remove.
     */
    fun remove(player: ElytraPlayer) {
        players.remove(player)

        if (players.isEmpty()) {
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
    private fun updateBoard(score: Int, time: Instant) {
        val formattedTime = DateTimeFormatter.ofPattern(Config.CONFIG.getString("time-format"))
            .withZone(ZoneOffset.UTC)
            .format(time)

        players.forEach { it.updateBoard(score, formattedTime, seed) }
    }

    protected open val score
        get() = max(0, (players[0].position.x - island.blockSpawn.x).toInt())

    protected val time: Instant
        get() = Instant.now().minusMillis(start?.toEpochMilli() ?: Instant.now().toEpochMilli())

    private var resetUp = false

    /**
     * Ticks the generator.
     */
    protected open fun tick() {
        val idx = sections.keys.max()
        val section = sections[idx]!!

        val player = players[0]
        val pos = player.position
        val score = score

        updateBoard(score, time)
        updateInfo()

        if (start == null && score > 0) {
            start = Instant.now()
        }

        if (score > 5 && !player.player.isGliding) {
            reset()
            return
        }

        if (section.isNearKnot(pos, 0) && section.end.y < 25) {
            val cloned = section.clone(Vector(0, 200, 0))

            sections[idx + 1] = cloned

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

        if (sections.size > 2) {
            val last = sections.minBy { it.key }

            clear(last.key, last.value)
        }
    }

    /**
     * Generates the next knot.
     */
    protected open fun generate() {
        if (sections.isEmpty()) {
            val section = Section(island.blockSpawn.clone().add(Vector(0, pointType.heightOffset, 0)), random)

            sections[0] = section

            section.generate(settings, pointType)

            return
        }

        val latest = sections.maxBy { it.key }
        val idx = latest.key
        val previous = latest.value
        val end = previous.end

        val section = Section(end, random)

        sections[idx + 1] = section

        section.generate(settings, pointType)
    }

    protected open fun clear(idx: Int, section: Section) {
        section.clear()

        sections.remove(idx)
    }

    /**
     * Resets the players and knots.
     */
    protected open fun reset(regenerate: Boolean = true, s: Int = ThreadLocalRandom.current().nextInt(0, SEED_BOUND)) {
        players.forEach {
            leaderboard.update(
                it.uuid, Score(
                    name = it.name,
                    score = score,
                    time = time.toEpochMilli(),
                    seed = seed
                )
            )
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

        player.player.teleportAsync(player.player.location.add(0.0, 200.0, 0.0))

        Task.create(IEP.instance)
            .delay(2)
            .execute { player.player.velocity = velocity }
            .run()

        resetUp = false
    }

    private fun updateInfo() {
        if (!settings.info) return

        players.forEach {
            it.sendActionBar(getFormattedSpeed(it))
        }
    }

    /**
     * Returns the current speed of the player in m/s.
     * @param player The player to get the speed for.
     * @return The current speed.
     */
    fun getSpeed(player: ElytraPlayer): Double = player.player.velocity.clone().setY(0).length() * 20

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
        settings = mapper.invoke(settings)
    }

    companion object {

        private const val SEED_BOUND = 1_000_000

        /**
         * Creates a new generator.
         * @param player The player to create the generator for.
         */
        fun create(player: Player,
                   leaderboard: Leaderboard,
                   pointType: PointType = PointType.CIRCLE,
                   gen: () -> Generator) {
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