package dev.efnilite.iep.generator

import dev.efnilite.iep.ElytraPlayer
import dev.efnilite.iep.IEP
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.schematic.Schematic
import dev.efnilite.vilib.schematic.Schematics
import dev.efnilite.vilib.util.Task
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.time.Instant
import java.time.format.DateTimeFormatter

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

    private val players = mutableListOf<ElytraPlayer>()
    private val rings = mutableMapOf<Int, Ring>()

    private var start: Instant = Instant.now()
    private lateinit var task: BukkitTask

    private lateinit var island: Island
    private val heading = Vector(1, 0, 0)

    /**
     * Adds a player to the generator.
     * @param player The player to add.
     */
    fun add(player: ElytraPlayer) {
        players.add(player)

        player.teleport(island.playerSpawn)
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
     * @param vector The vector to spawn the island at.
     */
    fun start(vector: Vector) {
        this.island = Island(vector, Schematics.getSchematic(IEP.instance, "spawn-island"))

        rings[0] = Ring(heading, island.blockSpawn, 0)
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
        val time = DateTimeFormatter.ofPattern("HH:mm:ss:SSS").format(timeMs)
        players.forEach { it.updateBoard(score, time) }
    }

    /**
     * Ticks the generator.
     */
    private fun tick() {
        val latest = rings.maxBy { it.key }
        val idx = latest.key
        val ring = latest.value

        val pos = players[0].position

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
        players.forEach { it.teleport(island.playerSpawn) }

        rings.forEach { (_, ring) -> ring.blocks.forEach { it.toLocation(World.world).block.type = Material.AIR } }
        rings.clear()
    }
}