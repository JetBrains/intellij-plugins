// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import com.intellij.ide.util.gotoByName.GotoSymbolModel2
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.navigation.NavigationItem
import com.intellij.testFramework.TestIndexingModeSupporter
import com.intellij.testFramework.TestIndexingModeSupporter.IndexingMode
import com.intellij.webSymbols.testFramework.checkListByFile
import junit.framework.Test
import junit.framework.TestSuite
import org.angular2.Angular2TestCase

class Angular2GotoSymbolTest : Angular2TestCase("navigation/symbol/", false), TestIndexingModeSupporter {

  companion object {
    @JvmStatic
    fun suite(): Test {
      val suite = TestSuite()
      suite.addTestSuite(Angular2GotoSymbolTest::class.java)
      TestIndexingModeSupporter.addTest(Angular2GotoSymbolTest::class.java, TestIndexingModeSupporter.FullIndexSuite(), suite);
      return suite
    }
  }

  private var indexingMode: IndexingMode  = IndexingMode.SMART

  override fun setIndexingMode(mode: IndexingMode) {
    this.indexingMode = mode
  }

  override fun getIndexingMode(): IndexingMode = indexingMode

  private fun checkGotoSymbol(name: String, vararg modules: WebFrameworkTestModule, detailed: Boolean = true) {
    doConfiguredTest(*modules) {
      indexingMode.setUpTest(myFixture.project, myFixture.testRootDisposable)
      val model = GotoSymbolModel2(getProject(), testRootDisposable)
      assertContainsElements(listOf(*model.getNames(false)), name)
      val actual = LinkedHashSet<String>()
      for (o in model.getElementsByName(name, false, "")) {
        if (o is NavigationItem) {
          val presentation = o.getPresentation()
          assertNotNull(presentation)
          actual.add(presentation!!.getPresentableText() + " " + presentation.locationString +
                     if (detailed) " - $o" else "")
        }
      }
      checkListByFile(actual.toList(), "${testName}.goto-list.txt", false)
    }
  }

  fun testElementSelector() = checkGotoSymbol("app-my-table")

  fun testAttributeSelector() = checkGotoSymbol("app-my-table")

  fun testAttrAndElementSelector() = checkGotoSymbol("app-my-table")

  fun testPipe() = checkGotoSymbol("foo", detailed = false)
}
