// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie

import com.intellij.grazie.ide.GrazieInspection
import com.intellij.grazie.ide.msg.GrazieStateLifecycle
import com.intellij.grazie.jlanguage.Lang
import com.intellij.grazie.utils.filterFor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPlainText
import com.intellij.spellchecker.inspections.SpellCheckingInspection
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl

abstract class GrazieTestBase : BasePlatformTestCase() {
  override fun getBasePath() = "contrib/grazie/src/test/testData"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(*inspectionTools)
    // Markdown changed PSI during highlighting
    (myFixture as? CodeInsightTestFixtureImpl)?.canChangeDocumentDuringHighlighting(true)

    if (!isSettingsLoad) {
      isSettingsLoad = true
      GrazieConfig.update { state ->
        // remove dialects
        state.update(enabledLanguages = Lang.values().filter {
          it !in listOf(Lang.CANADIAN_ENGLISH, Lang.BRITISH_ENGLISH,
                        Lang.AUSTRIAN_GERMAN, Lang.PORTUGAL_PORTUGUESE)
        }.toSet())
      }

      while (ApplicationManager.getApplication().messageBus.hasUndeliveredEvents(GrazieStateLifecycle.topic)) {
        Thread.sleep(100)
      }
    }
  }

  protected open fun runHighlightTestForFile(file: String) {
    myFixture.configureByFile(file)
    myFixture.checkHighlighting(true, false, false)
  }

  fun plain(vararg texts: String) = plain(texts.toList())

  fun plain(texts: List<String>): Collection<PsiElement> {
    return texts.flatMap { myFixture.configureByText("${it.hashCode()}.txt", it).filterFor<PsiPlainText>() }
  }

  companion object {
    private var isSettingsLoad = false
    val inspectionTools by lazy { arrayOf(GrazieInspection(), SpellCheckingInspection()) }
  }
}
