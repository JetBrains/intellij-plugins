// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.deprecated

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.psi.html.HtmlTag
import com.intellij.testFramework.UsefulTestCase
import com.intellij.webSymbols.testFramework.resolveReference
import com.intellij.webSymbols.testFramework.resolveWebSymbolReference
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.inspections.AngularUndefinedBindingInspection
import org.angular2.Angular2TestUtil

@Deprecated("Use test appropriate for IDE feature being tested - e.g. completion/resolve/highlighting ")
class Angular2NgContentSelectorsTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return Angular2TestUtil.getBaseTestDataPath() + "deprecated/ngContentSelectors"
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
    Angular2TestUtil.assertUnresolvedReference("<div f<caret>oo", myFixture)
  }
}
