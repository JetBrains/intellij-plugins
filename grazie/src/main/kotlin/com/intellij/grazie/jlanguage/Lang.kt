// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.jlanguage

import com.intellij.grazie.GrazieDynamic
import com.intellij.grazie.remote.RemoteLangDescriptor
import org.languagetool.language.Language

@Suppress("unused")
enum class Lang(val displayName: String, val className: String, val unicodeBlock: LangUnicodeBlock, val remote: RemoteLangDescriptor) {
  BRITISH_ENGLISH("English (GB)", "BritishEnglish", LangUnicodeBlock.ENGLISH, RemoteLangDescriptor.ENGLISH),
  AMERICAN_ENGLISH("English (US)", "AmericanEnglish", LangUnicodeBlock.ENGLISH, RemoteLangDescriptor.ENGLISH),
  CANADIAN_ENGLISH("English (Canadian)", "CanadianEnglish", LangUnicodeBlock.ENGLISH, RemoteLangDescriptor.ENGLISH),
  GERMANY_GERMAN("German (Germany)", "GermanyGerman", LangUnicodeBlock.GERMAN, RemoteLangDescriptor.GERMAN),
  AUSTRIAN_GERMAN("German (Austria)", "AustrianGerman", LangUnicodeBlock.GERMAN, RemoteLangDescriptor.GERMAN),
  PORTUGAL_PORTUGUESE("Portuguese (Portugal)", "PortugalPortuguese", LangUnicodeBlock.PORTUGUESE, RemoteLangDescriptor.PORTUGUESE),
  BRAZILIAN_PORTUGUESE("Portuguese (Brazil)", "BrazilianPortuguese", LangUnicodeBlock.PORTUGUESE, RemoteLangDescriptor.PORTUGUESE),
  SPANISH("Spanish", "Spanish", LangUnicodeBlock.SPANISH, RemoteLangDescriptor.SPANISH),
  RUSSIAN("Russian", "Russian", LangUnicodeBlock.RUSSIAN, RemoteLangDescriptor.RUSSIAN),
  FRENCH("French", "French", LangUnicodeBlock.FRENCH, RemoteLangDescriptor.FRENCH),
  ITALIAN("Italian", "Italian", LangUnicodeBlock.ITALIAN, RemoteLangDescriptor.ITALIAN),
  DUTCH("Dutch", "Dutch", LangUnicodeBlock.DUTCH, RemoteLangDescriptor.DUTCH),
  JAPANESE("Japanese", "Japanese", LangUnicodeBlock.JAPANESE, RemoteLangDescriptor.JAPANESE),
  CHINESE("Chinese", "Chinese", LangUnicodeBlock.CHINESE, RemoteLangDescriptor.CHINESE),
  PERSIAN("Persian", "Persian", LangUnicodeBlock.PERSIAN, RemoteLangDescriptor.PERSIAN),
  POLISH("Polish", "Polish", LangUnicodeBlock.POLISH, RemoteLangDescriptor.POLISH),
  GREEK("Greek", "Greek", LangUnicodeBlock.GREEK, RemoteLangDescriptor.GREEK),
  ROMANIAN("Romanian", "Romanian", LangUnicodeBlock.ROMANIAN, RemoteLangDescriptor.ROMANIAN),
  SLOVAK("Slovak", "Slovak", LangUnicodeBlock.SLOVAK, RemoteLangDescriptor.SLOVAK),
  UKRAINIAN("Ukrainian", "Ukrainian", LangUnicodeBlock.UKRAINIAN, RemoteLangDescriptor.UKRAINIAN);

  companion object {
    operator fun get(lang: Language): Lang? = values().find { lang.name == it.displayName }
    // NOTE: dialects have same short code
    operator fun get(code: String): Lang? = values().find { it.shortCode == code }

    fun sortedValues() = values().sortedBy(Lang::displayName)
  }

  val shortCode: String
    get() = remote.shortCode

  private var _jLanguage: Language? = null
  val jLanguage: Language?
    get() = _jLanguage ?: GrazieDynamic.loadLang(this)?.also {
      _jLanguage = it
    }

  fun isEnglish() = shortCode == "en"

  override fun toString() = displayName
}

