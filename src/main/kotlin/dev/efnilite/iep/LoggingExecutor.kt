package dev.efnilite.iep
interface LoggingExecutor {

    fun info(message: String)

    fun error(message: String)

    fun stack(message: String, ex: Exception)
}
