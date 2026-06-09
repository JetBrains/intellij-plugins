// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import com.intellij.ide.util.gotoByName.GotoSymbolModel2
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.navigation.NavigationItem
import com.intellij.polySymbols.testFramework.checkListByFile
import com.intellij.testFramework.TestIndexingModeSupporter.IndexingMode
import org.angular2.Angular2TestCase
import org.angular2.TestNoService
import org.angular2.TestTsGoProxy
import org.junit.Test
import org.junit.runners.Parameterized

@TestNoService
@TestTsGoProxy
class Angular2GotoSymbolTest : Angular2TestCase("navigation/symbol/") {

  companion object {
    @com.intellij.testFramework.Parameterized.Parameters(name = "ServiceKind={0}, IndexingMode={1}")
    @JvmStatic
    fun data(clazz: Class<*>): Collection<Any> =
      Angular2TestCase.data(clazz).flatMap { serviceParams ->
        listOf(IndexingMode.SMART, IndexingMode.DUMB_FULL_INDEX).map { indexingMode ->
          arrayOf((serviceParams as Array<*>)[0], indexingMode)
        }
      }
  }

  @JvmField
  @Parameterized.Parameter(1)
  var indexingMode: IndexingMode = IndexingMode.SMART

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

  @Test
  fun testElementSelector() = checkGotoSymbol("app-my-table")

  @Test
  fun testAttributeSelector() = checkGotoSymbol("app-my-table")

  @Test
  fun testAttrAndElementSelector() = checkGotoSymbol("app-my-table")

  @Test
  fun testPipe() = checkGotoSymbol("foo", detailed = false)
}
