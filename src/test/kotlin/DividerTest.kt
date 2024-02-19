import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.world.Divider
import org.bukkit.util.Vector
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DividerTest {

    @AfterEach
    fun clear() {
        Divider.clear()
    }

    @Test
    fun testAdd() {
        assertEquals(0, Divider.add(Generator()))
        assertEquals(1, Divider.add(Generator()))
        assertEquals(2, Divider.add(Generator()))
    }

    @Test
    fun testRemove() {
        val generator = Generator()

        Divider.add(generator)
        Divider.add(Generator())
        Divider.add(Generator())

        Divider.remove(generator)

        assertEquals(0, Divider.add(Generator()))
        assertEquals(3, Divider.add(Generator()))
    }

    @Test
    fun testToLocation() {
        val generator1 = Generator()
        val generator2 = Generator()
        val generator3 = Generator()
        val generator4 = Generator()

        Divider.add(generator1)
        Divider.add(generator2)
        Divider.add(generator3)
        Divider.add(generator4)

        val x = Divider.SIZE.x.toInt()
        val y = Divider.SIZE.y.toInt()
        val z = Divider.SIZE.z.toInt()

        assertEquals(Vector(0, y, 0), Divider.toLocation(generator1))
        assertEquals(Vector(x * 1, y, 0), Divider.toLocation(generator2))
        assertEquals(Vector(x * 1, y, z * 1), Divider.toLocation(generator3))
        assertEquals(Vector(0, y, z * 1), Divider.toLocation(generator4))
    }
}