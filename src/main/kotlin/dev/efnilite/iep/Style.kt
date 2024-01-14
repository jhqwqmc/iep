package dev.efnilite.iep

import org.bukkit.Material
import org.jetbrains.annotations.Contract

class Style {

    @Contract(pure = true)
    fun next(): Material {
        return listOf(Material.RED_WOOL, Material.RED_CONCRETE,
            Material.REDSTONE_BLOCK, Material.NETHER_BRICK)
            .random()
    }

}