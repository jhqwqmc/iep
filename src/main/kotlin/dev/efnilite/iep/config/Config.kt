package dev.efnilite.iep.config

import dev.efnilite.iep.IEP
import dev.efnilite.vilib.configupdater.ConfigUpdater
import org.bukkit.configuration.file.YamlConfiguration
import java.io.Reader

enum class Config(private val file: String) {

    CONFIG("config.yml"),
    REWARDS("rewards.yml");

    private lateinit var config: YamlConfiguration

    /**
     * Returns a boolean from the file.
     */
    fun getBoolean(path: String): Boolean = config.getBoolean(path)

    /**
     * Returns an integer from the file.
     */
    fun getInt(path: String, bounds: (Int) -> Boolean = { true }): Int {
        val value = config.getInt(path)

        if (!bounds.invoke(value)) {
            IEP.logging.error("Value $value at path $path is invalid")
        }

        return value
    }

    /**
     * Returns a string from the file.
     */
    fun getString(path: String, bounds: (String) -> Boolean = { true }): String {
        val value = config.getString(path)!!

        if (!bounds.invoke(value)) {
            IEP.logging.error("Value $value at path $path is invalid")
        }

        return value
    }

    /**
     * Returns a string list from the file.
     */
    fun getStringList(path: String, bounds: (List<String>) -> Boolean = { true }): List<String> {
        val value = config.getStringList(path)

        if (!bounds.invoke(value)) {
            IEP.logging.error("Value $value at path $path is invalid")
        }

        return value
    }

    /**
     * Returns a double from the file.
     */
    fun getDouble(path: String, bounds: (Double) -> Boolean = { true }): Double {
        val value = config.getDouble(path)

        if (!bounds.invoke(value)) {
            IEP.logging.error("Value $value at path $path is invalid")
        }

        return value
    }

    fun getPaths(path: String): Set<String> = config.getConfigurationSection(path)!!.getKeys(false)

    companion object {
        var saver: ConfigSaver = ConfigSaverImpl

        fun init() {
            for (config in entries) {
                saver.save(config.file)

                config.config = YamlConfiguration.loadConfiguration(saver.read(config.file))
            }
        }
    }
}

private object ConfigSaverImpl : ConfigSaver {

    override fun save(file: String) {
        IEP.instance.saveFile(file)

        ConfigUpdater.update(IEP.instance, file, IEP.instance.dataFolder.resolve(file), listOf("styles", "score", "interval", "one-time"))
    }

    override fun read(file: String): Reader {
        return IEP.instance.dataFolder.resolve(file).reader()
    }
}