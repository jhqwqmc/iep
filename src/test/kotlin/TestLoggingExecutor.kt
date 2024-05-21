import dev.efnilite.iep.LoggingExecutor

object TestLoggingExecutor : LoggingExecutor {

    override fun info(message: String) {
        println(message)
    }

    override fun error(message: String) {
        println(message)
    }

    override fun stack(message: String, ex: Exception) {
        println(message)
    }
}