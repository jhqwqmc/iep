package dev.efnilite.iep

import org.bukkit.configuration.file.YamlConfiguration

enum class Config(file: String) {

    CONFIG("config.yml");

    private val config: YamlConfiguration

    init {
        IEP.instance.saveResource(file, false)

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
}