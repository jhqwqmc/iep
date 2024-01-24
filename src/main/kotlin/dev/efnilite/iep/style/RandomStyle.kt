package dev.efnilite.iep.style

import org.bukkit.Material
import org.jetbrains.annotations.Contract

/**
 * Represents a style where each block is selected randomly.
 */
data class RandomStyle(val name: String, val data: List<Material>) : Style {

    @Contract(pure = true)
    override fun next() = data.random()

    override fun name() = name

}
