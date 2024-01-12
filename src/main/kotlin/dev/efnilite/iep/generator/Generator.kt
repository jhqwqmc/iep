package dev.efnilite.iep.generator

import dev.efnilite.iep.ElytraPlayer
import dev.efnilite.iep.IEP
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.util.Task
import org.bukkit.Material
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import org.jetbrains.annotations.Contract
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private data class Ring(val heading: Vector, val center: Vector, val radius: Int) {

    init {
        assert(radius >= 0)
        assert(heading.isNormalized)
    }

    val blocks = getBlocks()

    /**
     * Returns a list of vectors in a circle around [center] with radius [radius].
     * @return A list of vectors in a circle.
     */
    @Contract(pure = true)
    fun getBlocks(): List<Vector> {
        if (radius == 0) {
            return emptyList()
        }

        val blocks = mutableListOf<Vector>()
        val centerX = if (heading.x == 0.0) center.x.toInt() else center.z.toInt()
        val centerY = center.y.toInt()

        val accuracy = 30
        var t = 0.0
        repeat(accuracy) {
            t += 2 * Math.PI / accuracy

            val x = (centerX + radius * cos(t)).toInt()
            val y = (centerY + radius * sin(t)).toInt()

            blocks.add(Vector(if (heading.z == 0.0) x else 0, y, if (heading.x == 0.0) x else 0))
        }

        return blocks
    }

    /**
     * Returns whether the given vector is near the ring's center.
     * @param vector The vector to check.
     * @return Whether the given vector is near the ring's center.
     */
    @Contract(pure=true)
    fun isNear(vector: Vector): Boolean {
        val x = if (heading.x == 0.0) Pair(center.x, vector.x) else Pair(center.z, vector.z)
        val dx = abs(x.first - x.second)
        val dy = abs(center.y - vector.y)

        return dy <= radius - 1 && dx <= 2
    }
}

class Generator {

    private val players = mutableListOf<ElytraPlayer>()
    private val rings = mutableMapOf<Int, Ring>()

    private var start: Instant = Instant.now()
    private lateinit var task: BukkitTask

    private lateinit var playerSpawn: Vector
    private val heading = Vector(1, 0, 0)

    /**
     * Adds a player to the generator.
     * @param player The player to add.
     */
    fun add(player: ElytraPlayer) {
        players.add(player)

        player.teleport(playerSpawn)
    }

    /**
     * Removes a player from the generator.
     * @param player The player to remove.
     */
    fun remove(player: ElytraPlayer) {
        players.remove(player)
    }

    /**
     * Initializes all the stuff.
     * @param playerSpawn The spawn location of the player.
     * @param blockSpawn The spawn location of the first block.
     */
    fun start(playerSpawn: Vector, blockSpawn: Vector) {
        this.playerSpawn = playerSpawn

        rings[0] = Ring(heading, blockSpawn, 0)
        generate()

        task = Task.create(IEP.instance)
            .repeat(1)
            .execute {
                if (players.isEmpty()) {
                    task.cancel()
                    return@execute
                }

                tick()
            }
            .run()
    }

    /**
     * Updates all players' scoreboards.
     */
    private fun updateBoard(score: Int) {
        val timeMs = Instant.now().minusMillis(start.toEpochMilli())
        val time = DateTimeFormatter.ofPattern("HH:mm:ss").format(timeMs)
        players.forEach { it.updateBoard(score, time) }
    }

    /**
     * Ticks the generator.
     */
    private fun tick() {
        val latest = rings.maxBy { it.key }
        val idx = latest.key
        val ring = latest.value

        val pos = players[0].location

        updateBoard(idx - 1)

        if (ring.isNear(pos)) {
            generate()

            if (idx - 1 == 0) {
                start = Instant.now()
            }

            return
        }

        if (ring.center.distance(pos) > 100) {
            reset()
            return
        }
    }

    /**
     * Generates the next ring.
     */
    private fun generate() {
        val latest = rings.maxBy { it.key }
        val idx = latest.key
        val ring = latest.value

        val next = ring.center.clone().add(heading.clone().multiply(30))

        val nextRing = Ring(heading, next, ring.radius)
        rings[idx + 1] = nextRing

        nextRing.blocks.forEach { it.toLocation(World.world).block.type = Material.RED_CONCRETE }
    }

    /**
     * Resets the players and rings.
     */
    private fun reset() {
        players.forEach { it.teleport(playerSpawn) }

        rings.forEach { (_, ring) -> ring.blocks.forEach { it.toLocation(World.world).block.type = Material.AIR } }
        rings.clear()
    }
}