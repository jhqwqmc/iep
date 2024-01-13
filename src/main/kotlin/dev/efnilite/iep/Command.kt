package dev.efnilite.iep

import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.world.Divider
import dev.efnilite.vilib.command.ViCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Command : ViCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false

        when (args[0]) {
            "play" -> {
                val player = ElytraPlayer(sender as Player)

                player.setup()

                val generator = Generator()

                Divider.add(generator)

                generator.start(Divider.toLocation(generator))

                generator.add(player)
            }
        }

        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return emptyList()
    }
}
