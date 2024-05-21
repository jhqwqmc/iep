package dev.efnilite.iep.config

import java.io.Reader

interface ConfigSaver {

    fun save(file: String)

    fun read(file: String): Reader
}