// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import com.intellij.ide.util.gotoByName.GotoSymbolModel2
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.vfs.VirtualFileFilter
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.testFramework.UsefulTestCase
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angularjs.AngularTestUtil

class Angular2GotoSymbolTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "navigation/symbol/" + getTestName(true)
  }

  fun testElementSelector() {
    myFixture.configureByFiles("my-table.component.ts", "package.json")
    doTest("app-my-table", true,
           "app-my-table (MyTableComponent, my-table.component.ts) - my-table.component.ts [342]")
  }

  fun testAttributeSelector() {
    myFixture.configureByFiles("my-table.component.ts", "package.json")
    doTest("app-my-table", true,
           "app-my-table (MyTableComponent, my-table.component.ts) - my-table.component.ts [343]")
  }

  fun testAttrAndElementSelector() {
    myFixture.configureByFiles("my-table.component.ts", "package.json")
    doTest("app-my-table", true,
           "app-my-table (MyTableComponent, my-table.component.ts) - my-table.component.ts [342]",
           "app-my-table (MyTableComponent, my-table.component.ts) - my-table.component.ts [355]")
  }

  fun testPipe() {
    myFixture.configureByFiles("foo.pipe.ts", "package.json")
    doTest("foo", false,
           "foo (foo.pipe.ts)")
  }

  private fun doTest(name: String, detailed: Boolean, vararg expectedItems: String) {
    (myFixture.getPsiManager() as PsiManagerEx).setAssertOnFileLoadingFilter(VirtualFileFilter.ALL, myFixture.testRootDisposable)
    val model = GotoSymbolModel2(myFixture.getProject(), myFixture.testRootDisposable)
    UsefulTestCase.assertContainsElements(listOf(*model.getNames(false)), name)
    val actual = ArrayList<String>()
    for (o in model.getElementsByName(name, false, "")) {
      if (o is NavigationItem) {
        val presentation = o.getPresentation()
        assertNotNull(presentation)
        actual.add(presentation!!.getPresentableText() + " " + presentation.locationString +
                   if (detailed) " - $o" else "")
      }
    }
    UsefulTestCase.assertSameElements(actual, *expectedItems)
  }
}
