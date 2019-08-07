package tanvd.grazi.language

import org.languagetool.JLanguageTool
import org.languagetool.Language
import tanvd.grazi.GraziBundle
import tanvd.grazi.GraziPlugin
import tanvd.grazi.remote.LangToolLibDescriptor

@Suppress("unused")
enum class Lang(val displayName: String, val shortCode: String, private val className: String, val descriptor: LangToolLibDescriptor,
                private val enabledRules: Set<String> = emptySet(),
                private val disabledRules: Set<String> = emptySet(),
                private val disabledCategories: Set<String> = emptySet()) {
    BRITISH_ENGLISH("English (GB)", "en", "BritishEnglish", LangToolLibDescriptor.ENGLISH,
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    AMERICAN_ENGLISH("English (US)", "en", "AmericanEnglish", LangToolLibDescriptor.ENGLISH,
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    CANADIAN_ENGLISH("English (Canadian)", "en", "CanadianEnglish", LangToolLibDescriptor.ENGLISH,
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    RUSSIAN("Russian", "ru", "Russian", LangToolLibDescriptor.RUSSIAN, GraziBundle.langConfig("ru.rules.enabled")),
    PERSIAN("Persian", "fa", "Persian", LangToolLibDescriptor.PERSIAN),
    FRENCH("French", "fr", "French", LangToolLibDescriptor.FRENCH),
    GERMANY_GERMAN("German (Germany)", "de", "GermanyGerman", LangToolLibDescriptor.GERMAN),
    AUSTRIAN_GERMAN("German (Austria)", "de", "AustrianGerman", LangToolLibDescriptor.GERMAN),
    POLISH("Polish", "pl", "Polish", LangToolLibDescriptor.POLISH),
    ITALIAN("Italian", "it", "Italian", LangToolLibDescriptor.ITALIAN),
    DUTCH("Dutch", "nl", "Dutch", LangToolLibDescriptor.DUTCH),
    PORTUGAL_PORTUGUESE("Portuguese (Portugal)", "pt", "PortugalPortuguese", LangToolLibDescriptor.PORTUGUESE),
    BRAZILIAN_PORTUGUESE("Portuguese (Brazil)", "pt", "BrazilianPortuguese", LangToolLibDescriptor.PORTUGUESE),
    CHINESE("Chinese", "zh", "Chinese", LangToolLibDescriptor.CHINESE),
    GREEK("Greek", "el", "Greek", LangToolLibDescriptor.GREEK),
    JAPANESE("Japanese", "ja", "Japanese", LangToolLibDescriptor.JAPANESE),
    ROMANIAN("Romanian", "ro", "Romanian", LangToolLibDescriptor.ROMANIAN),
    SLOVAK("Slovak", "sk", "Slovak", LangToolLibDescriptor.SLOVAK),
    SPANISH("Spanish", "es", "Spanish", LangToolLibDescriptor.SPANISH),
    UKRAINIAN("Ukrainian", "uk", "Ukrainian", LangToolLibDescriptor.UKRAINIAN);

    companion object {
        operator fun get(lang: Language): Lang? = values().find { lang.name == it.displayName }
        operator fun get(code: String): Lang? = values().find { it.shortCode == code }

        fun sortedValues() = values().sortedBy(Lang::displayName)
    }

    private var _jLanguage: Language? = null
    val jLanguage: Language?
        get() {
            if (_jLanguage == null) {
                _jLanguage = GraziPlugin.loadClass("org.languagetool.language.$className")?.newInstance() as Language?
            }

            return _jLanguage
        }

    fun isEnglish() = shortCode == "en"

    override fun toString() = displayName

    fun configure(tool: JLanguageTool) {
        tool.allRules.filter { rule -> enabledRules.any { rule.id.contains(it) } }.forEach {
            tool.enableRule(it.id)
        }

        tool.allRules.filter { rule ->
            disabledRules.any { rule.id.contains(it) } || disabledCategories.any { rule.category.id.toString().contains(it) }
        }.forEach {
            tool.disableRule(it.id)
        }
    }
}

