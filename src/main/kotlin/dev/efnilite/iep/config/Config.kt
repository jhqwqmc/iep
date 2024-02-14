package dev.efnilite.iep.config

import dev.efnilite.iep.IEP
import dev.efnilite.vilib.configupdater.ConfigUpdater
import org.bukkit.configuration.file.YamlConfiguration

enum class Config(file: String) {

    CONFIG("config.yml");

    private val config: YamlConfiguration

    init {
        IEP.instance.saveFile(file)

        ConfigUpdater.update(IEP.instance, file, IEP.instance.dataFolder.resolve(file), /*TODO listOf("styles")*/)

        config = YamlConfiguration.loadConfiguration(IEP.instance.dataFolder.resolve(file))
    }

    /**
     * Returns a boolean from the file.
     */
    fun getBoolean(path: String): Boolean = config.getBoolean(path)

    /**
     * Returns an integer from the file.
     */
    fun getInt(path: String, bounds: (Int) -> Boolean = { true }): Int {
        val value = config.getInt(path)

        // todo replace require
        require(bounds.invoke(value))

        return value
    }

    /**
     * Returns a string from the file.
     */
    fun getString(path: String, bounds: (String) -> Boolean = { true }): String {
        val value = config.getString(path)!!

        require(bounds.invoke(value))

        return value
    }

    /**
     * Returns a string list from the file.
     */
    fun getStringList(path: String, bounds: (List<String>) -> Boolean = { true }): List<String> {
        val value = config.getStringList(path)

        require(bounds.invoke(value))

        return value
    }

    /**
     * Returns a double from the file.
     */
    fun getDouble(path: String, bounds: (Double) -> Boolean = { true }): Double {
        val value = config.getDouble(path)

        require(bounds.invoke(value))

        return value
    }

    fun getPaths(path: String): Set<String> = config.getConfigurationSection(path)!!.getKeys(false)
}