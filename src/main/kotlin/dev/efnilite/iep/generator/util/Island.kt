package dev.efnilite.iep.generator.util

import dev.efnilite.iep.world.World
import dev.efnilite.vilib.schematic.Schematic
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.util.Vector

class Island(vector: Vector, schematic: Schematic) {

    private val blocks: List<Block> = schematic.paste(vector.toLocation(World.world))
    val playerSpawn: Vector
    val blockSpawn: Vector

    init {
        assert(blocks.isNotEmpty())

        blocks.first { it.type == Material.DIAMOND_BLOCK }.let {
            playerSpawn = it.location.toVector().add(Vector(0.5, 0.0, 0.5))

            it.type = Material.AIR
        }
        blocks.first { it.type == Material.EMERALD_BLOCK }.let {
            blockSpawn = it.location.toVector().add(Vector(5, 0, 0))

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
