package org.angular2.codeInsight

import com.intellij.webSymbols.checkDocumentationAtCaret
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.modules.Angular2TestModule
import org.angular2.modules.Angular2TestModule.Companion.configureCopy
import org.angular2.modules.Angular2TestModule.Companion.configureLink
import org.angularjs.AngularTestUtil

class Angular2DocumentationTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass) + "documentation"
  }

  fun testTagName() {
    doTest()
  }

  fun testSimpleInput() {
    doTest()
  }

  fun testSimpleInputBinding() {
    doTest()
  }

  fun testSimpleOutputBinding() {
    doTest()
  }

  fun testSimpleBananaBox() {
    doTest()
  }

  fun testDirectiveWithMatchingInput() {
    doTest()
  }

  fun testDirectiveWithoutMatchingInput() {
    doTest()
  }

  fun testGlobalAttribute() {
    doTest()
  }

  fun testFieldWithoutDocs() {
    doTest()
  }

  fun testFieldWithDocsPrivate() {
    doTest()
  }

  fun testExtendedEventKey() {
    doTest()
  }

  fun testCdkNoDataRow() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_CDK_14_2_0)
    myFixture.configureByFile(getTestName(true) + ".html")
    myFixture.checkDocumentationAtCaret()
  }

  fun testCdkNoDataRowNotImported() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CDK_14_2_0)
    myFixture.configureByFiles(getTestName(true) + ".html", getTestName(true) + ".ts")
    myFixture.checkDocumentationAtCaret()
  }

  fun testComponentDecorator() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_16_0_0_NEXT_4)
    myFixture.configureByFiles(getTestName(true) + ".ts")
    myFixture.checkDocumentationAtCaret()
  }

  fun testUnknownDirective() {
    doTest()
  }

  fun testDirectiveInputNoDoc() {
    myFixture.configureByFiles(getTestName(true) + ".ts", "package.json")
    myFixture.checkDocumentationAtCaret()
  }

  fun testDirectiveInOutNoDoc() {
    myFixture.configureByFiles(getTestName(true) + ".ts", "package.json")
    myFixture.checkDocumentationAtCaret()
  }

  fun testDirectiveNoDocInOutDoc() {
    myFixture.configureByFiles(getTestName(true) + ".ts", "package.json")
    myFixture.checkDocumentationAtCaret()
  }

  fun testDirectiveInOutMixedDoc() {
    myFixture.configureByFiles(getTestName(true) + ".ts", "package.json")
    myFixture.checkDocumentationAtCaret()
  }

  private fun doTest() {
    myFixture.configureByFiles(getTestName(true) + ".html",
                               "package.json", "deps/list-item.component.ts", "deps/ng_for_of.ts", "deps/ng_if.ts", "deps/dir.ts",
                               "deps/ng_plural.ts")
    myFixture.checkDocumentationAtCaret()
  }
}
