import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.iep.mode.Mode

object TestMode : Mode {

    override val name = "test"

    override val leaderboard = Leaderboard("test")

    override fun getGenerator() = Generator()

}