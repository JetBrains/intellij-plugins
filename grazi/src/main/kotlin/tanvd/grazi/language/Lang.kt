// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.language

import org.languagetool.JLanguageTool
import org.languagetool.language.Language
import tanvd.grazi.GraziBundle
import tanvd.grazi.GraziPlugin
import tanvd.grazi.remote.RemoteLangDescriptor

@Suppress("unused")
enum class Lang(val displayName: String, val shortCode: String, private val className: String, val remote: RemoteLangDescriptor,
                private val enabledRules: Set<String> = emptySet(),
                private val disabledRules: Set<String> = emptySet(),
                private val disabledCategories: Set<String> = emptySet()) {
    BRITISH_ENGLISH("English (GB)", "en", "BritishEnglish", RemoteLangDescriptor.ENGLISH,
        GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    AMERICAN_ENGLISH("English (US)", "en", "AmericanEnglish", RemoteLangDescriptor.ENGLISH,
        GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    CANADIAN_ENGLISH("English (Canadian)", "en", "CanadianEnglish", RemoteLangDescriptor.ENGLISH,
        GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    GERMANY_GERMAN("German (Germany)", "de", "GermanyGerman", RemoteLangDescriptor.GERMAN, disabledCategories = GraziBundle.langConfig("de.categories.disabled")),
    AUSTRIAN_GERMAN("German (Austria)", "de", "AustrianGerman", RemoteLangDescriptor.GERMAN, disabledCategories = GraziBundle.langConfig("de.categories.disabled")),
    PORTUGAL_PORTUGUESE("Portuguese (Portugal)", "pt", "PortugalPortuguese", RemoteLangDescriptor.PORTUGUESE),
    BRAZILIAN_PORTUGUESE("Portuguese (Brazil)", "pt", "BrazilianPortuguese", RemoteLangDescriptor.PORTUGUESE),
    SPANISH("Spanish", "es", "Spanish", RemoteLangDescriptor.SPANISH),
    RUSSIAN("Russian", "ru", "Russian", RemoteLangDescriptor.RUSSIAN, GraziBundle.langConfig("ru.rules.enabled")),
    FRENCH("French", "fr", "French", RemoteLangDescriptor.FRENCH),
    ITALIAN("Italian", "it", "Italian", RemoteLangDescriptor.ITALIAN),
    DUTCH("Dutch", "nl", "Dutch", RemoteLangDescriptor.DUTCH),
    JAPANESE("Japanese", "ja", "Japanese", RemoteLangDescriptor.JAPANESE),
    CHINESE("Chinese", "zh", "Chinese", RemoteLangDescriptor.CHINESE),
    PERSIAN("Persian", "fa", "Persian", RemoteLangDescriptor.PERSIAN),
    POLISH("Polish", "pl", "Polish", RemoteLangDescriptor.POLISH),
    GREEK("Greek", "el", "Greek", RemoteLangDescriptor.GREEK),
    ROMANIAN("Romanian", "ro", "Romanian", RemoteLangDescriptor.ROMANIAN),
    SLOVAK("Slovak", "sk", "Slovak", RemoteLangDescriptor.SLOVAK),
    UKRAINIAN("Ukrainian", "uk", "Ukrainian", RemoteLangDescriptor.UKRAINIAN);

    companion object {
        operator fun get(lang: Language): Lang? = values().find { lang.name == it.displayName }
        // NOTE: dialects have same short code
        operator fun get(code: String): Lang? = values().find { it.shortCode == code }

        fun sortedValues() = values().sortedBy(Lang::displayName)
    }

    private var _jLanguage: Language? = null
    val jLanguage: Language?
        get() = _jLanguage ?: (GraziPlugin.loadClass("org.languagetool.language.$className")?.newInstance() as Language?)?.also {
            _jLanguage = it
        }

    fun isEnglish() = shortCode == "en"

    override fun toString() = displayName

    fun configure(tool: JLanguageTool) {
        tool.allRules.filter { rule -> enabledRules.any { rule.id.contains(it) } }.forEach {
            tool.enableRule(it.id)
        }

        // disable all spellchecker rules also (use them directly in GraziSpellchecker)
        tool.allRules.filter { rule ->
            rule.isDictionaryBasedSpellingRule || disabledRules.any { rule.id.contains(it) }
                || disabledCategories.any { rule.category.id.toString().contains(it) }
        }.forEach {
            tool.disableRule(it.id)
        }
    }
}

