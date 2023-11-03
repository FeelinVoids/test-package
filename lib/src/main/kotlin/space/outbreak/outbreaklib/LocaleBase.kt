package space.outbreak.outbreaklib

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.ComponentSerializer
import org.apache.commons.text.StringSubstitutor
import org.bukkit.entity.Player
import kotlin.reflect.full.declaredMemberProperties


abstract class LocaleBase(
    val serializer: ComponentSerializer<Component, Component, String> = MiniMessage.miniMessage(),
    val parseStaticPlaceholders: Boolean = true
) {
    class L() {
        lateinit var path: String
            private set

        private lateinit var locale: LocaleBase

        private val values = mutableMapOf<String?, String>()

        constructor(path: String) : this() {
            this.path = path
        }

        internal fun pathOr(or: String): String {
            if (this::path.isInitialized)
                return path
            return or
        }

        internal fun init(locale: LocaleBase, path: String, lang: String?, value: String) {
            if (!this::path.isInitialized)
                this.path = path
            this.locale = locale
            values[lang] = value
        }

        internal fun clear() {
            values.clear()
        }
        
        fun comp(lang: String? = locale.defaultLang, vararg replacing: Pair<String, Any>): Component {
            return locale.process(raw(lang), lang, *replacing)
        }

        fun comp(vararg replacing: Pair<String, Any>): Component {
            return locale.process(raw(locale.defaultLang), locale.defaultLang, *replacing)
        }

        fun compOrNull(lang: String? = locale.defaultLang, vararg replacing: Pair<String, Any>): Component? {
            return rawOrNull(lang)?.let { locale.process(it, lang, *replacing) }
        }

        fun compOrNull(vararg replacing: Pair<String, Any>): Component? {
            return rawOrNull(locale.defaultLang)?.let { locale.process(it, locale.defaultLang, *replacing) }
        }

        fun raw(lang: String? = locale.defaultLang, vararg placeholders: Pair<String, Any>): String {
            return locale.replaceAll(raw(lang), mapOf(*placeholders))
        }

        private fun raw(lang: String? = locale.defaultLang): String {
            return values.getOrDefault(lang, values.getOrDefault(null, path))
        }

        fun rawOrNull(lang: String? = locale.defaultLang): String? {
            return values[lang]
        }

        fun rawOrNull(lang: String? = locale.defaultLang, vararg placeholders: Pair<String, Any>): String? {
            return rawOrNull(lang)?.let { locale.replaceAll(it, mapOf(*placeholders)) }
        }

        fun send(audience: Audience, vararg replacing: Pair<String, Any>) {
            if (audience is Player)
                audience.sendMessage(comp(audience.locale().language, *replacing))
            else
                audience.sendMessage(comp(null, *replacing))
        }

        fun sendActionBar(audience: Audience, vararg replacing: Pair<String, Any>) {
            if (audience is Player)
                audience.sendActionBar(comp(audience.locale().language, *replacing))
            else
                audience.sendActionBar(comp(null, *replacing))
        }
    }

    /**
     * Оборачивает компонент в компонент с явно отключенным курсивом.
     * Полезно, чтобы убирать курсив из описаний и названий предметов.
     * */
    fun deitalize(comp: Component): Component {
        val dn = Component.empty().decoration(TextDecoration.ITALIC, false)
        return dn.children(mutableListOf(comp))
    }

    fun processAndDeitalize(text: String, vararg placeholders: Pair<String, Any>): Component {
        return deitalize(process(text, defaultLang, *placeholders))
    }

    fun processAndDeitalize(
        text: String,
        lang: String? = defaultLang,
        vararg placeholders: Pair<String, Any>
    ): Component {
        return deitalize(process(text, lang, *placeholders))
    }

    private val languages = mutableSetOf<String?>()

    /**
     * Язык по умолчанию. Если в языках существует язык null, он будет
     * использоваться по умолчанию. Иначе - будет использоваться первый в списке язык.
     * */
    var defaultLang: String? = null
        private set

    private val placeholders = mutableMapOf<String?, MutableMap<String, Any?>>()

    private fun valueByPath(config: Map<*, *>, path: String): Any? {
        return valueByPath(config, path.split("."))
    }

    private fun valueByPath(current: Map<*, *>, path: List<String>): Any? {
        if (path.size > 1) {
            val subStep = current[path[0]]
            if (subStep !is Map<*, *>) {
                return null
            }
            return valueByPath(subStep, path.subList(1, path.size))
        }
        return current[path[0]]
    }

    fun load(config: Map<*, *>, lang: String? = null) {
        this::class.declaredMemberProperties.forEach { prop ->
            val l = prop.call(this)
            if (l !is L) return@forEach
            val path: String = l.pathOr(prop.name.replace("__", ".").replace("_", "-").lowercase())
            val value = (valueByPath(config, path) as String?) ?: path
            l.init(this, path, lang, value)
        }

        if (parseStaticPlaceholders)
            for (ph in (config.getOrDefault("placeholders", mapOf<String, String>()) as Map<*, *>).entries) {
                val k = ph.key
                if (k !is String) continue
                if (lang !in placeholders)
                    placeholders[lang] = mutableMapOf()
                placeholders[lang]!![k] = ph.value.toString()
            }

        languages.add(lang)

        defaultLang = if (null in languages) null else languages.first()
    }

    /** Удаляет все загруженные данные */
    fun clear() {
        this::class.declaredMemberProperties.forEach { prop ->
            val l = prop.call(this)
            if (l !is L) return@forEach
            l.clear()
        }
        languages.clear()
        defaultLang = null
        placeholders.clear()
    }

    /**
     * Подставляет плейсхолдеры из карты `plceholders` в строку `text`,
     * возвращает получившуюся строку.
     *
     * Для больших количеств плейсхолдеров это производительнее, чем просто `replace()`
     * */
    fun replaceAll(str: String, placeholders: Map<String, Any?>): String {
        val substitutor = StringSubstitutor(placeholders, "%", "%", '\\')
        return substitutor.replace(str)
    }

    private fun _process(text: String, mapStrings: Map<String, Any?>, mapComps: Map<String, Component>): Component {
        var comp = serializer.deserialize(replaceAll(text, mapStrings))

        for (entry in mapComps.iterator()) {
            comp = comp.replaceText(
                TextReplacementConfig.builder()
                    .matchLiteral("%${entry.key}%")
                    .replacement(entry.value)
                    .build()
            )
        }

        return comp
    }

    /**
     * Парсит строку формата MiniMessage в компонент.
     *
     * @param text строка для перевода в компонент
     * @param replacing плейсхолдеры для замены, где ключ - имя плейсхолдера
     *  без %. Значение - Component либо любой другой объект. Компоненты будут
     *  вставлены, используя TextReplacementConfig (медленно),
     *  объекты любого другого типа - просто переведены в строку и
     *  заменены (оптимизированно)
     * */
    fun process(text: String, lang: String? = defaultLang, vararg replacing: Pair<String, Any>): Component {
        val mapComps = mutableMapOf<String, Component>()
        val phs = placeholders[lang] ?: placeholders[defaultLang]
        val mapStrings: MutableMap<String, Any?> = phs?.toMutableMap() ?: mutableMapOf()

        for (pair in replacing) {
            if (pair.second is Component)
                mapComps[pair.first] = pair.second as Component
            else
                mapStrings[pair.first] = pair.second
        }

        return _process(text, mapStrings, mapComps)
    }

    /**
     * Парсит строку формата MiniMessage в компонент.
     *
     * @param text строка для перевода в компонент
     * @param replacing плейсхолдеры для замены, где ключ - имя плейсхолдера
     *  без %. Значение - Component либо любой другой объект. Компоненты будут
     *  вставлены, используя TextReplacementConfig (медленно),
     *  объекты любого другого типа - просто переведены в строку и
     *  заменены (оптимизированно)
     * */
    fun process(text: String, lang: String? = defaultLang, replacing: Map<String, Any>): Component {
        val mapComps = mutableMapOf<String, Component>()
        val phs = placeholders[lang] ?: placeholders[defaultLang]
        val mapStrings: MutableMap<String, Any?> = phs?.toMutableMap() ?: mutableMapOf()

        replacing.forEach { (key, value) ->
            if (value is Component)
                mapComps[key] = value
            else
                mapStrings[key] = value
        }

        return _process(text, mapStrings, mapComps)
    }

    /**
     * Парсит строку формата MiniMessage в компонент.
     *
     * @param text строка для перевода в компонент
     * @param replacing плейсхолдеры для замены, где ключ - имя плейсхолдера
     *  без %. Значение - Component либо любой другой объект. Компоненты будут
     *  вставлены, используя TextReplacementConfig (медленно),
     *  объекты любого другого типа - просто переведены в строку и
     *  заменены (оптимизированно)
     * */
    fun process(text: String, vararg replacing: Pair<String, Any>): Component {
        return process(text, null, *replacing)
    }

    /**
     * Парсит строку формата MiniMessage в компонент.
     *
     * @param text строка для перевода в компонент
     * @param replacing плейсхолдеры для замены, где ключ - имя плейсхолдера
     *  без %. Значение - Component либо любой другой объект. Компоненты будут
     *  вставлены, используя TextReplacementConfig (медленно),
     *  объекты любого другого типа - просто переведены в строку и
     *  заменены (оптимизированно)
     * */
    fun process(text: String, replacing: Map<String, Any>): Component {
        return process(text, null, replacing)
    }
}

