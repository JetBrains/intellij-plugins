// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.psi.html.HtmlTag
import com.intellij.testFramework.UsefulTestCase
import com.intellij.webSymbols.resolveReference
import com.intellij.webSymbols.resolveWebSymbolReference
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.inspections.AngularUndefinedBindingInspection
import org.angularjs.AngularTestUtil

class Angular2NgContentSelectorsTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "codeInsight/ngContentSelectors"
  }

  fun testHighlightingSource() {
    myFixture.configureByFiles("highlighting.html", "component.ts", "package.json")
    doTestHighlighting()
  }

  fun testHighlightingPureIvy() {
    myFixture.copyDirectoryToProject("node_modules/ivy-lib", "node_modules/ivy-lib")
    myFixture.configureByFiles("highlighting.html", "package.json")
    doTestHighlighting()
  }

  fun testHighlightingMixedIvy() {
    myFixture.copyDirectoryToProject("node_modules/mixed-lib", "node_modules/mixed-lib")
    myFixture.configureByFiles("highlighting.html", "package.json")
    doTestHighlighting()
  }

  fun testHighlightingMetadata() {
    myFixture.copyDirectoryToProject("node_modules/metadata-lib", "node_modules/metadata-lib")
    myFixture.configureByFiles("highlighting.html", "package.json")
    doTestHighlighting()
  }

  private fun doTestHighlighting() {
    myFixture.enableInspections(HtmlUnknownAttributeInspection::class.java,
                                HtmlUnknownTagInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.checkHighlighting()
  }

  fun testResolutionSource() {
    myFixture.configureByFiles("resolution.html", "component.ts", "package.json")
    for (test in listOf(
      Pair("<fo<caret>o b>", "foo,[bar]"),
      Pair("<fo<caret>o c>", "foo,[bar]"),
      Pair("<div b<caret>ar", "foo,[bar]"),
      Pair("<bar f<caret>oo", "bar[foo]"),
      Pair("<span g<caret>oo", ":not([goo])")
    )) {
      try {
        assertEquals(test.second, myFixture.resolveWebSymbolReference(test.first).psiContext!!.getText())
      }
      catch (error: AssertionError) {
        throw AssertionError("Failed with signature: " + test.first, error)
      }
    }
    UsefulTestCase.assertInstanceOf(myFixture.resolveReference("<fo<caret>o a>"), HtmlTag::class.java)
    UsefulTestCase.assertInstanceOf(myFixture.resolveReference("<go<caret>o"), HtmlTag::class.java)
    AngularTestUtil.assertUnresolvedReference("<div f<caret>oo", myFixture)
  }
}
