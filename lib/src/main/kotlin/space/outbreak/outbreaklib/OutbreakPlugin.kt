package space.outbreak.outbreaklib

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

abstract class OutbreakPlugin : JavaPlugin() {
    val mapper: ObjectMapper = YAMLMapper.builder()
        .configure(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, true)
        .build()
        .registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    /** Получает файл из ресурсов, распаковывая его в папку конфигов плагина */
    fun getConfigFile(filename: String): File {
        val file = File(dataFolder, filename)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            saveResource(filename, false)
        }
        return file
    }

    /**
     * Находит в ресурсах файл [resourcePath], распаковывает его в папку
     * плагина, читает его как yaml и парсит в объект типа [type]
     * */
    fun <T> readConfig(resourcePath: String, type: Class<T>): T {
        return mapper.readValue(getConfigFile(resourcePath), type)
    }
}