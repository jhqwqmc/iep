package dev.efnilite.iep.style

import org.bukkit.Material

interface Style {

    /**
     * Returns the next block data.
     */
    fun next(): Material

}