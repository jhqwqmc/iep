package dev.efnilite.iep

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.efnilite.iep.hook.PapiHook
import dev.efnilite.iep.mode.*
import dev.efnilite.iep.style.IncrementalStyle
import dev.efnilite.iep.style.RandomStyle
import dev.efnilite.iep.style.Style
import dev.efnilite.iep.world.Divider
import dev.efnilite.iep.world.World
import dev.efnilite.vilib.ViPlugin
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.schematic.Schematics
import dev.efnilite.vilib.util.Logging
import dev.efnilite.vilib.util.Task
import dev.efnilite.vilib.util.elevator.GitElevator
import org.bukkit.Material
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class IEP : ViPlugin() {

    val logging = Logging(this)

    override fun enable() {
        instance = this

        registerListener(Events())
        registerCommand("iep", Command())

        saveFile("schematics/spawn-island")

        World.create()
        Menu.init(this)

        Schematics.addFromFiles(this,
            *Files.list(Path.of(dataFolder.toString(), "schematics"))
                .map { it.toFile() }.toList().toTypedArray())

        registerStyle("styles.random") { name, data -> RandomStyle(name, data) }
        registerStyle("styles.incremental") { name, data -> IncrementalStyle(name, data) }

        registerMode(DefaultMode)
        registerMode(SpeedDemonMode)
        registerMode(MinSpeedMode)
        registerMode(TimeTrialMode)
        registerMode(CloseMode)
        registerMode(ObstacleMode)

        if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            PapiHook.register()
        }

        Task.create(this)
            .async()
            .repeat(3 * 60 * 20)
            .execute { modes.forEach { it.leaderboard.write() } }
            .run()
    }

    fun saveFile(path: String) {
        val file = File(dataFolder.toString(), path)

        if (!file.exists()) {
            saveResource(path, false)
        }
    }

    private fun registerStyle(path: String, fn: (name: String, data: List<Material>) -> Style) {
        Config.CONFIG.getPaths(path).forEach { name ->
            registerStyle(
                fn.invoke(name, Config.CONFIG.getStringList("$path.$name")
                .map {
                    try {
                        return@map Material.getMaterial(it.uppercase())!!
                    } catch (ex: NullPointerException) {
                        logging.error("Invalid material in style $path.$name: $it")
                        return@map Material.STONE
                    }
                })
            )
        }
    }

    override fun disable() {
        Divider.generators.forEach { generator -> generator.players.forEach { generator.remove(it) } }

        World.delete()
    }

    override fun getElevator(): GitElevator? = null

    companion object {
        val GSON: Gson = GsonBuilder().disableHtmlEscaping().create()

        lateinit var instance: IEP
            private set

        private val modes: MutableList<Mode> = mutableListOf()

        fun registerMode(mode: Mode) = modes.add(mode)

        fun getMode(name: String): Mode? = modes.firstOrNull { it.name == name }

        fun getModes() = modes.toList()

        private val styles: MutableList<Style> = mutableListOf()

        fun registerStyle(style: Style) = styles.add(style)

        fun getStyle(name: String) = styles.first { it.name() == name }

        fun getStyles() = styles.toList()

        fun String.toTitleCase(): String {
            return this.split(" ").joinToString(" ") { it.uppercase() }
        }
    }
}