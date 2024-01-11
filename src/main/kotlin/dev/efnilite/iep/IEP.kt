package dev.efnilite.iep

import dev.efnilite.iep.world.World
import dev.efnilite.vilib.ViPlugin
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.util.Logging
import dev.efnilite.vilib.util.elevator.GitElevator

class IEP : ViPlugin() {

    val logging = Logging(this)

    override fun enable() {
        instance = this

        World.create()
        Menu.init(this)
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