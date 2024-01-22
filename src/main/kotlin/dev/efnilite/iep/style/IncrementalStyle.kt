package dev.efnilite.iep.style

import org.bukkit.block.data.BlockData

/**
 * Represents a style where each block is selected incrementally.
 */
data class IncrementalStyle(val data: List<BlockData>) : Style {

    private var idx = 0

    override fun next(): BlockData {
        val next = data[idx]

        idx++
        if (idx >= data.size) {
            idx = 0
        }

        return next
    }
}