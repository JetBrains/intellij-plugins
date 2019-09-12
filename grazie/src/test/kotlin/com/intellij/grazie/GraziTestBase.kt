// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPlainText
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.grazie.ide.GrazieInspection
import com.intellij.grazie.ide.msg.GrazieStateLifecycle
import com.intellij.grazie.language.Lang
import com.intellij.grazie.utils.filterFor

abstract class GraziTestBase(private val withSpellcheck: Boolean) : LightJavaCodeInsightFixtureTestCase() {
  override fun getBasePath() = "contrib/grazie/src/test/testData"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(*inspectionTools)

    GrazieConfig.update { state ->
      // remove dialects
      state.update(enabledLanguages = Lang.values().filter {
        it !in listOf(Lang.CANADIAN_ENGLISH, Lang.BRITISH_ENGLISH, Lang.AUSTRIAN_GERMAN, Lang.PORTUGAL_PORTUGUESE)
      }.toSet(), enabledSpellcheck = withSpellcheck)
    }

    while (ApplicationManager.getApplication().messageBus.hasUndeliveredEvents(GrazieStateLifecycle.topic)) {
      Thread.sleep(500)
    }
  }

  protected fun runHighlightTestForFile(file: String) {
    myFixture.configureByFile(file)

    // Markdown changed PSI during highlighting
    (myFixture as? CodeInsightTestFixtureImpl)?.canChangeDocumentDuringHighlighting(true)

    myFixture.testHighlighting(true, false, false, file)
  }

  fun plain(vararg texts: String) = plain(texts.toList())

  fun plain(texts: List<String>): Collection<PsiElement> {
    return texts.flatMap { myFixture.configureByText("${it.hashCode()}.txt", it).filterFor<PsiPlainText>() }
  }


  companion object {
    private val inspectionTools by lazy { arrayOf(GrazieInspection()) }
  }
}
