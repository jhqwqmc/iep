package dev.efnilite.iep

import dev.efnilite.iep.generator.Generator
import dev.efnilite.iep.menu.PlayMenu
import dev.efnilite.vilib.command.ViCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Command : ViCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.isEmpty() || sender !is Player) return false

        when (args[0]) {
            "play" -> PlayMenu.open(sender)
            "leave" -> Generator.remove(sender)
        }

        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return emptyList()
    }
}
