package dev.efnilite.iep.generator

import dev.efnilite.iep.Config
import dev.efnilite.iep.IEP
import dev.efnilite.iep.leaderboard.Score
import dev.efnilite.iep.player.ElytraPlayer
import dev.efnilite.iep.player.ElytraPlayer.Companion.asElytraPlayer
import dev.efnilite.iep.world.Divider
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.schematic.Schematic
import dev.efnilite.vilib.schematic.Schematics
import dev.efnilite.vilib.util.Task
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max

private class Island(vector: Vector, schematic: Schematic) {

    val blocks: List<Block> = schematic.paste(vector.toLocation(World.world))
    val playerSpawn: Vector
    val blockSpawn: Vector

    init {
        assert(blocks.isNotEmpty())

        blocks.first { it.type == Material.DIAMOND_BLOCK }.let {
            playerSpawn = it.location.toVector()

            it.type = Material.AIR
        }
        blocks.first { it.type == Material.EMERALD_BLOCK }.let {
            blockSpawn = it.location.toVector()

            it.type = Material.AIR
        }
    }

    /**
     * Clears the island.
     */
    fun clear() {
        blocks.forEach { it.type = Material.AIR }
    }
}

class Generator {

    var settings: Settings = Settings(IEP.getStyles().random(), 5)
        private set

    val players = mutableListOf<ElytraPlayer>()
    private val sections = mutableMapOf<Int, Section>()

    private var start: Instant? = Instant.now()
    private lateinit var task: BukkitTask

    private lateinit var island: Island
    private val heading = Vector(1, 0, 0)

    private var seed: Int = ThreadLocalRandom.current().nextInt(0, SEED_BOUND)
    private var random = Random()

    private val leaderboard = IEP.getLeaderboard("default")

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
        }
    }

    /**
     * Initializes all the stuff.
     * @param vector The vector to spawn the island at.
     */
    fun start(vector: Vector) {
        island = Island(vector, Schematics.getSchematic(IEP.instance, "spawn-island"))

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

    fun getScore() = max(0, (players[0].position.x - island.blockSpawn.x).toInt())

    fun getTime() = Instant.now().minusMillis(start?.toEpochMilli() ?: Instant.now().toEpochMilli())

    private var resetUp = false

    /**
     * Ticks the generator.
     */
    private fun tick() {
        val idx = sections.keys.max()
        val section = sections[idx]!!

        val player = players[0]
        val pos = player.position
        val score = getScore()

        updateBoard(score, getTime())

        if (start == null && score > 0) {
            start = Instant.now()
        }

        if (score > 5 && !player.player.isGliding) {
            reset()
            return
        }

        if (section.isNear(pos, 0) && section.end.y < 25) {
            val cloned = section.clone(Vector(0, 200, 0))

            sections[idx + 1] = cloned

            cloned.generate(settings)

            resetUp = true

            return
        }

        if (resetUp) {
            val previous = sections[idx - 1]!!

            if (!previous.isNear(pos)) {
                return
            }

            val velocity = player.player.velocity

            player.player.teleportAsync(player.player.location.add(0.0, 200.0, 0.0))

            Task.create(IEP.instance)
                .delay(1)
                .execute { player.player.velocity = velocity }
                .run()

            resetUp = false

            return
        }

        if (section.isNear(pos)) {
            generate()

            return
        }
    }

    /**
     * Generates the next knot.
     */
    private fun generate() {
        if (sections.isEmpty()) {
            val section = Section(island.blockSpawn, random)

            sections[0] = section

            section.generate(settings)

            return
        }

        val latest = sections.maxBy { it.key }
        val idx = latest.key
        val previous = latest.value
        val end = previous.end

        val section = Section(end, random)

        sections[idx + 1] = section

        section.generate(settings)
    }

    /**
     * Resets the players and knots.
     */
    private fun reset(regenerate: Boolean = true) {
        players.forEach {
            leaderboard.update(
                it.uuid, Score(
                    name = it.name,
                    score = getScore(),
                    time = getTime().toEpochMilli(),
                    seed = seed
                )
            )
        }


        players.forEach { it.teleport(island.playerSpawn) }

        seed = ThreadLocalRandom.current().nextInt(0, SEED_BOUND)
        random = Random(seed.toLong())
        start = null
        resetUp = false

        AsyncBuilder(sections.values
            .flatMap { it.blocks }
            .associateWith { Material.AIR })

        sections.clear()

        if (!regenerate) return

        generate()
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
        fun create(player: Player) {
            val elytraPlayer = ElytraPlayer(player)

            elytraPlayer.join()

            val generator = Generator()

            Divider.add(generator)

            generator.add(elytraPlayer)

            generator.start(Divider.toLocation(generator))
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