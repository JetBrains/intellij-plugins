// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.fus

import com.intellij.internal.statistic.eventLog.FeatureUsageData
import com.intellij.internal.statistic.service.fus.collectors.FUCounterUsageLogger
import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.language.Lang
import com.intellij.grazie.utils.isSpellingTypo

object GrazieFUCounterCollector {
  fun languageDetected(lang: Lang?) = log("language.detected") {
    addData("language", lang?.shortCode ?: "")
  }

  fun typoFound(typo: Typo) = log("typo.found") {
    addData("id", typo.info.rule.id)
    addData("fixes", typo.fixes.size)
    addData("spellcheck", typo.isSpellingTypo)
  }

  fun quickfixApplied(ruleId: String, cancelled: Boolean, isSpellcheck: Boolean) = log("quickfix.applied") {
    addData("id", ruleId)
    addData("cancelled", cancelled)
    addData("spellcheck", isSpellcheck)
  }

  private fun log(eventId: String, body: FeatureUsageData.() -> Unit) = FUCounterUsageLogger.getInstance()
    .logEvent("grazi.count", eventId,FeatureUsageData().apply(body))
}
