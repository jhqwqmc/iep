import dev.efnilite.iep.generator.RingDirector
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

//        todo
//        assertEquals(director.next(), Vector(36, -2, -8))
//        assertEquals(director.next(), Vector(26, 16, -19))
//        assertEquals(director.next(), Vector(46, 14, 15))
    }
}