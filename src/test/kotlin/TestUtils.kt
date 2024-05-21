import dev.efnilite.iep.IEP
import dev.efnilite.iep.config.Config
import org.bukkit.entity.Player
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

object TestUtils {

    private fun setup() {
        IEP.logging = TestLoggingExecutor
        Config.saver = TestConfigSaver
        Config.init()
    }

    fun getPlayer(): Player {
        setup()

        val player = mock(Player::class.java)

        `when`(player.name).thenReturn("player")

        return player
    }

}