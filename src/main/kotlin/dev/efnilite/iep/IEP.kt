package dev.efnilite.iep

import dev.efnilite.iep.leaderboard.Leaderboard
import dev.efnilite.iep.style.RandomStyle
import dev.efnilite.iep.style.Style
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.ViPlugin
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.schematic.Schematics
import dev.efnilite.vilib.util.Logging
import dev.efnilite.vilib.util.Task
import dev.efnilite.vilib.util.elevator.GitElevator
import org.bukkit.Material
import java.nio.file.Files
import java.nio.file.Path

class IEP : ViPlugin() {

    val logging = Logging(this)

    override fun enable() {
        instance = this

        registerListener(Events())
        registerCommand("iep", Command())

        saveResource("schematics/spawn-island", false)

        World.create()
        Menu.init(this)

        Schematics.addFromFiles(this,
            *Files.list(Path.of(dataFolder.toString(), "/schematics"))
                .map { it.toFile() }.toList().toTypedArray())

        register(Leaderboard("default"))

        registerStyle("styles.random")
        registerStyle("styles.incremental")

        Task.create(this)
            .async()
            .repeat(3 * 60 * 20)
            .execute {
                for (leaderboard in leaderboards) {
                    leaderboard.write()
                }
            }
            .run()
    }

    private fun registerStyle(path: String) {
        Config.CONFIG.getPaths(path).forEach { name ->
            register(RandomStyle(name, Config.CONFIG.getStringList("$path.$name")
                .map {
                    try {
                        return@map Material.getMaterial(it.uppercase())!!
                    } catch (ex: NullPointerException) {
                        logging.error("Invalid material in style $path.$name: $it")
                        return@map Material.STONE
                    }
                }))
        }
    }

    override fun disable() {
        World.delete()
    }

    override fun getElevator(): GitElevator? = null

    companion object {
        lateinit var instance: IEP
            private set

        private val leaderboards: MutableList<Leaderboard> = mutableListOf()

        fun register(leaderboard: Leaderboard) = leaderboards.add(leaderboard)

        fun getLeaderboard(name: String) = leaderboards.first { it.name == name }

        private val styles: MutableList<Style> = mutableListOf()

        fun register(style: Style) = styles.add(style)

        fun getStyle(name: String) = styles.first { it.name() == name }

        fun getStyles() = styles.toList()
    }
}