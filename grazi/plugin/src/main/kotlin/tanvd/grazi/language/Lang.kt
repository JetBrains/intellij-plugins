package tanvd.grazi.language

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.extensions.PluginId
import org.languagetool.JLanguageTool
import org.languagetool.Language
import tanvd.grazi.GraziBundle
import tanvd.grazi.GraziPlugin

@Suppress("unused")
enum class Lang(val displayName: String, val shortCode: String, val className: String,
                private val enabledRules: Set<String> = emptySet(),
                private val disabledRules: Set<String> = emptySet()) {
    BRITISH_ENGLISH("English (GB)", "en", "BritishEnglish",GraziBundle.langConfig("en.enabled"), GraziBundle.langConfig("en.disabled")),
    AMERICAN_ENGLISH("English (US)", "en", "AmericanEnglish", GraziBundle.langConfig("en.enabled"), GraziBundle.langConfig("en.disabled")),
    CANADIAN_ENGLISH("English (Canadian)", "en", "CanadianEnglish", GraziBundle.langConfig("en.enabled"), GraziBundle.langConfig("en.disabled")),
    RUSSIAN("Russian", "ru", "Russian", GraziBundle.langConfig("ru.enabled")),
    PERSIAN("Persian", "fa", "Persian"),
    FRENCH("French", "fr", "French"),
    GERMANY_GERMAN("German (Germany)", "de", "GermanyGerman"),
    AUSTRIAN_GERMAN("German (Austria)", "de", "AustrianGerman"),
    POLISH("Polish", "pl", "Polish"),
    ITALIAN("Italian", "it", "Italian"),
    DUTCH("Dutch", "nl", "Dutch"),
    PORTUGAL_PORTUGUESE("Portuguese (Portugal)", "pt", "PortugalPortuguese"),
    BRAZILIAN_PORTUGUESE("Portuguese (Brazil)", "pt", "BrazilianPortuguese"),
    CHINESE("Chinese", "zh", "Chinese"),
    GREEK("Greek", "el", "Greek"),
    JAPANESE("Japanese", "ja", "Japanese"),
    ROMANIAN("Romanian", "ro", "Romanian"),
    SLOVAK("Slovak", "sk", "Slovak"),
    SPANISH("Spanish", "es", "Spanish"),
    UKRAINIAN("Ukrainian", "uk", "Ukrainian");

    companion object {
        operator fun get(lang: Language): Lang? = values().find { lang.name == it.displayName }
        operator fun get(code: String): Lang? = values().find { it.shortCode == code }

        fun sortedValues() = values().sortedBy(Lang::displayName)
    }

    val jLanguage: Language by lazy {
        val loader = PluginManager.getPlugin(PluginId.getId(GraziPlugin.id))!!.pluginClassLoader
        Class.forName("org.languagetool.language.$className", true, loader).newInstance() as Language
    }

    fun isEnglish() = shortCode == "en"

    override fun toString() = displayName

    fun configure(tool: JLanguageTool) {
        tool.allRules.filter { rule -> enabledRules.any { rule.id.contains(it) } }.forEach {
            tool.enableRule(it.id)
        }

        tool.allRules.filter { rule -> disabledRules.any { rule.id.contains(it) } }.forEach {
            tool.disableRule(it.id)
        }
    }
}

