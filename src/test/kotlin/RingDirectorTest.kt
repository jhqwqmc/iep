import dev.efnilite.iep.generator.RingDirector
import org.bukkit.util.Vector
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class RingDirectorTest {

    private lateinit var random: Random

    @BeforeEach
    fun setup() {
        random = Random(1)
    }

    @Test
    fun testNext() {
        val director = RingDirector(random)

        assertEquals(director.next(), Vector(43, -1, -8))
        assertEquals(director.next(), Vector(38, 8, -19))
        assertEquals(director.next(), Vector(48, 7, 15))
    }
}