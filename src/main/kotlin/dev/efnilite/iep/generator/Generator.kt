package dev.efnilite.iep.generator

import dev.efnilite.iep.Config
import dev.efnilite.iep.IEP
import dev.efnilite.iep.leaderboard.Score
import dev.efnilite.iep.player.ElytraPlayer
import dev.efnilite.iep.player.ElytraPlayer.Companion.asElytraPlayer
import dev.efnilite.iep.style.Style
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
import kotlin.math.abs
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

    val players = mutableListOf<ElytraPlayer>()
    private val sections = mutableMapOf<Int, Section>()

    var style = IEP.getStyles()[0]
    private var start: Instant? = Instant.now()
    private lateinit var task: BukkitTask

    private lateinit var island: Island
    private val heading = Vector(1, 0, 0)

    private var seed: Int = abs(ThreadLocalRandom.current().nextInt())
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

    /**
     * Ticks the generator.
     */
    private fun tick() {
        val section = sections.maxBy { it.key }.value

        val pos = players[0].position
        val score = max(0, (pos.x - island.blockSpawn.x).toInt())
        val time = Instant.now().minusMillis(start?.toEpochMilli() ?: Instant.now().toEpochMilli())

        updateBoard(score, time)

        if (start == null && score > 0) {
            start = Instant.now()
        }

        if (section.isNear(pos)) {
            for (player in players) {
                leaderboard.update(
                    player.uuid, Score(
                        name = player.name,
                        score = score,
                        time = time.toEpochMilli(),
                        seed = seed
                    )
                )
            }

            generate()

            return
        }

        if (score > 5 && !players[0].player.isGliding) {
            reset()
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

            section.generate(style)

            return
        }

        val latest = sections.maxBy { it.key }
        val idx = latest.key
        val section = latest.value

        val new = Section(section.end, random)

        sections[idx + 1] = new

        new.generate(style)
    }

    /**
     * Resets the players and knots.
     */
    private fun reset(regenerate: Boolean = true) {
        players.forEach { it.teleport(island.playerSpawn) }

        seed = abs(ThreadLocalRandom.current().nextInt())
        random = Random(seed.toLong())
        start = null

        // todo cache blocks
        sections.values.forEach {
            AsyncSectionBuilder(
                it.points,
                object : Style {
                    override fun next() = Material.AIR
                    override fun name() = "air"
                },
                World.world
            ).run()
        }

        sections.clear()

        if (!regenerate) return

        generate()
    }

    companion object {

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