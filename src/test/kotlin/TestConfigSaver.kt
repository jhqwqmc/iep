import dev.efnilite.iep.config.ConfigSaver
import java.io.File
import java.io.FileOutputStream
import java.io.Reader

object TestConfigSaver : ConfigSaver {

    override fun save(file: String) {
        TestConfigSaver::class.java.getResourceAsStream("/$file")?.copyTo(FileOutputStream(File("target", file)))
    }

    override fun read(file: String): Reader {
        println(file)
        return "target/$file".reader()
    }
}