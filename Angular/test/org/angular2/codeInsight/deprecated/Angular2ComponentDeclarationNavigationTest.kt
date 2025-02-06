// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.deprecated

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.codeInsight.navigation.actions.GotoTypeDeclarationAction
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolService
import com.intellij.model.psi.impl.targetSymbols
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.UsefulTestCase
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TestUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@Deprecated("Use test appropriate for IDE feature being tested - e.g. go to declaration")
@RunWith(Parameterized::class)
class Angular2ComponentDeclarationNavigationTest : Angular2CodeInsightFixtureTestCase() {
  @Parameterized.Parameter
  @JvmField
  var myIsComponent = false

  @Parameterized.Parameter(value = 1)
  @JvmField
  var muIsInjected = false

  @Parameterized.Parameter(value = 2)
  @JvmField
  var myLocation: String? = null

  @Parameterized.Parameter(value = 3)
  @JvmField
  var myElementText: String? = null
  override fun getTestDataPath(): String {
    return Angular2TestUtil.getBaseTestDataPath() + "deprecated/component-navigation"
  }

  @Test
  fun testGoToDeclarationHandler() {
    doTest(GotoDeclarationAction(),
           if (muIsInjected) "customUsage.ts" else "customUsage.html",
           myLocation!!,
           if (myIsComponent) "Component Declaration" else "Directive Declaration",
           "custom.ts",
           myElementText)
  }

  @Test
  fun testTypeDeclarationHandler() {
    if (myIsComponent) {
      doTest(GotoTypeDeclarationAction(),
             if (muIsInjected) "customUsage.ts" else "customUsage.html",
             myLocation!!,
             "Component Template",
             "custom.html",
             "<div>custom</div>")
    }
    else {
      doTest(GotoTypeDeclarationAction(),
             if (muIsInjected) "customUsage.ts" else "customUsage.html",
             myLocation!!,
             null,
             if (muIsInjected) "customUsage.ts" else "customUsage.html",
             null)
    }
  }

  private fun doTest(action: AnAction,
                     testFile: String,
                     location: String,
                     actionLabel: String?,
                     targetFile: String?,
                     elementText: String?) {
    myFixture.configureByFiles(testFile, "custom.html", "custom.ts", "package.json")
    myFixture.moveToOffsetBySignature(location)
    val result = myFixture.testAction(action)
    assertEquals(actionLabel, result.text)
    val focusedEditor = FileEditorManager.getInstance(myFixture.getProject()).getSelectedTextEditor()
    val file = PsiDocumentManager.getInstance(myFixture.getProject()).getPsiFile(focusedEditor!!.getDocument())
    assertEquals(targetFile, file!!.getName())
    if (elementText == null) {
      return
    }
    val symbols: Collection<Symbol?> = targetSymbols(file, focusedEditor.getCaretModel().offset)
    UsefulTestCase.assertSize(1, symbols)
    val symbol = symbols.firstOrNull()
    if (symbol is WebSymbol) {
      UsefulTestCase.assertInstanceOf(symbol, WebSymbol::class.java)
      assertEquals(elementText, symbol.name)
    }
    else {
      val psiElement = PsiSymbolService.getInstance().extractElementFromSymbol(symbol!!)
      assertNotNull("Bad symbol class: " + symbol.javaClass, psiElement)
      assertEquals(elementText, psiElement!!.getText())
    }
  }

  companion object {
    @Parameterized.Parameters(name = "Component={0}, Injected={1}: {2}, {3}")
    @JvmStatic
    fun data(): Collection<Any> {
      return listOf(arrayOf(true, false, "<my-cu<caret>st", "my-customer"),
                    arrayOf(true, true, "<my-cu<caret>st", "my-customer"),
                    arrayOf(true, false, "my-cu<caret>stomer-att", "my-customer-attr"),
                    arrayOf(true, true, "my-cu<caret>stomer-att", "my-customer-attr"),
                    arrayOf(false, false, "foo-<caret>dir", "foo-directive"),
                    arrayOf(false, true, "foo-<caret>dir", "foo-directive"))
    }
  }
}
