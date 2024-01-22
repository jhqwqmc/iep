package dev.efnilite.iep.style

import org.bukkit.block.data.BlockData

interface Style {

    /**
     * Returns the next block data.
     */
    fun next(): BlockData

}