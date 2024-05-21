package reward

import TestMode
import TestUtils
import dev.efnilite.iep.reward.Reward
import dev.efnilite.iep.reward.RewardExecutor
import org.bukkit.entity.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class RewardTest {

    private lateinit var player: Player
    private lateinit var executor: RewardExecutor

    @BeforeEach
    fun setup() {
        player = TestUtils.getPlayer()

        executor = mock(RewardExecutor::class.java)
    }

    @Test
    fun testLeave() {
        val reward = Reward("leave||all||send||Hello, World!", executor)
        reward.execute(player, TestMode)

        verify(executor, never()).send(player, "Hello, World!")
    }

    @Test
    fun testOtherMode() {
        val reward = Reward("now||default||send||Hello, World!", executor)
        reward.execute(player, TestMode)

        verify(executor, never()).send(player, "Hello, World!")
    }

    @Test
    fun testSendMessage() {
        val reward = Reward("now||all||send||Hello, World!", executor)
        reward.execute(player, TestMode)

        verify(executor).send(player, "Hello, World!")
    }

    @Test
    fun testPlayerCommand() {
        val reward = Reward("now||all||player command||fly %player%", executor)
        reward.execute(player, TestMode)

        verify(executor).playerCommand(player, "fly ${player.name}")
    }

    @Test
    fun testConsoleCommand() {
        val reward = Reward("now||all||console command||give %player% diamond", executor)
        reward.execute(player, TestMode)

        verify(executor).consoleCommand("give ${player.name} diamond")
    }

    @Test
    fun testVault() {
        val reward = Reward("now||all||vault||1000", executor)
        reward.execute(player, TestMode)

        verify(executor).give(player, 1000.0)
    }

    @Test
    fun testInvalidVault() {
        val reward = Reward("now||all||vault||a", executor)
        reward.execute(player, TestMode)

        verify(executor, never()).give(player, 1000.0)
    }
}