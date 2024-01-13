package dev.efnilite.iep

import dev.efnilite.iep.world.World
import dev.efnilite.vilib.ViPlugin
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.schematic.Schematics
import dev.efnilite.vilib.util.Logging
import dev.efnilite.vilib.util.elevator.GitElevator
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
    }

    override fun disable() {
        World.delete()
    }

    override fun getElevator(): GitElevator? = null

    companion object {
        lateinit var instance: IEP
            private set
    }
}