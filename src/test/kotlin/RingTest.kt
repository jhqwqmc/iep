import dev.efnilite.iep.generator.Ring
import org.bukkit.util.Vector
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RingTest {

    private lateinit var ring: Ring

    @BeforeEach
    fun setup() {
        ring = Ring(Vector(1, 0, 0), Vector(0, 0, 0), 10)
    }

    @Test
    fun testBlocks() {
        TODO()
    }

    @Test
    fun testIsNear() {
        TODO()
    }
}