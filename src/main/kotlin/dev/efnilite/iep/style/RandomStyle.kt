package dev.efnilite.iep.style

import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.jetbrains.annotations.Contract

/**
 * Represents a style where each block is selected randomly.
 */
data class RandomStyle(val data: List<Material>) : Style {

    @Contract(pure = true)
    override fun next() = data.random()

}
