// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.jlanguage

import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.ide.fus.GrazieFUCounterCollector
import com.intellij.grazie.ide.msg.GrazieStateLifecycle
import tanvd.grazi.langdetect.detector.LanguageDetector
import tanvd.grazi.langdetect.detector.LanguageDetectorBuilder
import tanvd.grazi.langdetect.ngram.NgramExtractor
import tanvd.grazi.langdetect.profiles.LanguageProfileReader

object LangDetector : GrazieStateLifecycle {
  private const val charsForLangDetection = 1_000
  private lateinit var languages: Set<Lang>

  @Volatile
  private var detector: LanguageDetector? = null
    get() {
      if (field == null) {
        synchronized(this) {
          if (field == null) {
            init(GrazieConfig.get())
          }
        }
      }

      return field
    }

  fun getLang(text: String) = detector?.getProbabilities(text.take(charsForLangDetection))
    ?.maxBy { it.probability }
    ?.let { detectedLanguage -> languages.find { it.shortCode == detectedLanguage.locale.language } }
    .also { GrazieFUCounterCollector.languageDetected(it) }

  override fun init(state: GrazieConfig.State) {
    languages = state.availableLanguages
    val profiles = LanguageProfileReader().read(languages.filter { it.shortCode != "zh" }.map { it.shortCode } + "zh-CN").toSet()

    detector = LanguageDetectorBuilder.create(NgramExtractor.standard)
      .probabilityThreshold(0.90)
      .prefixFactor(1.5)
      .suffixFactor(2.0)
      .withProfiles(profiles)
      .build()
  }

  override fun update(prevState: GrazieConfig.State, newState: GrazieConfig.State) {
    if (prevState.availableLanguages != newState.availableLanguages) init(newState)
  }
}
