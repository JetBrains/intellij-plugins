// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.jlanguage

import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.ide.fus.GrazieFUCounterCollector
import com.intellij.grazie.ide.msg.GrazieStateLifecycle
import tanvd.grazie.langdetect.ngram.LanguageDetectorBuilder
import tanvd.grazie.langdetect.ngram.impl.ngram.NgramExtractor
import tanvd.grazie.langdetect.ngram.impl.profiles.LanguageProfileReader

object LangDetector : GrazieStateLifecycle {
  private var available: Set<Lang>? = null
    get() {
      //Required for Inspection Integration Tests and possibly other tests
      if (field == null) init(GrazieConfig.get())
      return field
    }

  private val detector by lazy {
    LanguageDetectorBuilder(NgramExtractor.standard)
      .minimalConfidence(0.90)
      .prefixFactor(1.5)
      .suffixFactor(2.0)
      .withProfiles(LanguageProfileReader.readAllBuiltIn())
      .build()
  }

  /**
   * Get natural language of text.
   *
   * It will perform NGram and Rule-based search for possible languages.
   *
   * @return Language that is detected.
   */
  fun getLanguage(text: String) = detector.detect(text.take(1_000)).preferred
    .also { GrazieFUCounterCollector.languageDetected(it) }

  /**
   * Get natural language of text, if it is enabled in Grazie
   *
   * @return Lang that is detected and enabled in grazie
   */
  fun getAvailableLang(text: String) = getLanguage(text).let {
    available!!.find { lang -> lang.equalsTo(it) }
  }

  override fun init(state: GrazieConfig.State) {
    available = state.availableLanguages
  }

  override fun update(prevState: GrazieConfig.State, newState: GrazieConfig.State) {
    if (prevState.availableLanguages != newState.availableLanguages) init(newState)
  }
}
