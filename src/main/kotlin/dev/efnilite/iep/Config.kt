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
    fun getBoolean(path: String) = config.getBoolean(path)
}