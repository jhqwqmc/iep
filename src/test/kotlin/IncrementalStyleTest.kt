import dev.efnilite.iep.style.IncrementalStyle
import org.bukkit.Material
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IncrementalStyleTest {

    @Test
    fun testNext() {
        val style = IncrementalStyle("test", listOf(Material.STONE, Material.DIRT, Material.GRASS_BLOCK))

        assertEquals(Material.STONE, style.next())
        assertEquals(Material.DIRT, style.next())
        assertEquals(Material.GRASS_BLOCK, style.next())
        assertEquals(Material.STONE, style.next())
    }

}