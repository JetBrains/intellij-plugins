// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.language

import org.languagetool.JLanguageTool
import org.languagetool.language.Language
import com.intellij.grazie.GrazieBundle
import com.intellij.grazie.GrazieDynamic
import com.intellij.grazie.remote.RemoteLangDescriptor

@Suppress("unused")
enum class Lang(val displayName: String, val shortCode: String, val className: String, val remote: RemoteLangDescriptor,
                private val enabledRules: Set<String> = emptySet(),
                private val disabledRules: Set<String> = emptySet(),
                private val disabledCategories: Set<String> = emptySet()) {
  BRITISH_ENGLISH("English (GB)", "en", "BritishEnglish", RemoteLangDescriptor.ENGLISH,
                  GrazieBundle.langConfig("en.rules.enabled"), GrazieBundle.langConfig("en.rules.disabled"),
                  GrazieBundle.langConfig("en.categories.disabled")),
  AMERICAN_ENGLISH("English (US)", "en", "AmericanEnglish", RemoteLangDescriptor.ENGLISH,
                   GrazieBundle.langConfig("en.rules.enabled"), GrazieBundle.langConfig("en.rules.disabled"),
                   GrazieBundle.langConfig("en.categories.disabled")),
  CANADIAN_ENGLISH("English (Canadian)", "en", "CanadianEnglish", RemoteLangDescriptor.ENGLISH,
                   GrazieBundle.langConfig("en.rules.enabled"), GrazieBundle.langConfig("en.rules.disabled"),
                   GrazieBundle.langConfig("en.categories.disabled")),
  GERMANY_GERMAN("German (Germany)", "de", "GermanyGerman", RemoteLangDescriptor.GERMAN,
                 disabledCategories = GrazieBundle.langConfig("de.categories.disabled")),
  AUSTRIAN_GERMAN("German (Austria)", "de", "AustrianGerman", RemoteLangDescriptor.GERMAN,
                  disabledCategories = GrazieBundle.langConfig("de.categories.disabled")),
  PORTUGAL_PORTUGUESE("Portuguese (Portugal)", "pt", "PortugalPortuguese", RemoteLangDescriptor.PORTUGUESE),
  BRAZILIAN_PORTUGUESE("Portuguese (Brazil)", "pt", "BrazilianPortuguese", RemoteLangDescriptor.PORTUGUESE),
  SPANISH("Spanish", "es", "Spanish", RemoteLangDescriptor.SPANISH),
  RUSSIAN("Russian", "ru", "Russian", RemoteLangDescriptor.RUSSIAN, GrazieBundle.langConfig("ru.rules.enabled")),
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
    get() = _jLanguage ?: GrazieDynamic.loadLang(this)?.also {
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

