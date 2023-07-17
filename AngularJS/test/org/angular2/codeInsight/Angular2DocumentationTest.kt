package org.angular2.codeInsight

import com.intellij.webSymbols.checkDocumentationAtCaret
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2DocumentationTest : Angular2TestCase("documentation") {

  fun testTagName() = doTestWithDeps()

  fun testSimpleInput() = doTestWithDeps()

  fun testSimpleInputBinding() = doTestWithDeps()

  fun testSimpleOutputBinding() = doTestWithDeps()

  fun testSimpleBananaBox() = doTestWithDeps()

  fun testDirectiveWithMatchingInput() = doTestWithDeps()

  fun testDirectiveWithoutMatchingInput() = doTestWithDeps()

  fun testGlobalAttribute() = doTestWithDeps()

  fun testFieldWithoutDocs() = doTestWithDeps()

  fun testFieldWithDocsPrivate() = doTestWithDeps()

  fun testExtendedEventKey() = doTestWithDeps()

  fun testCdkNoDataRow() = doTest(Angular2TestModule.ANGULAR_CDK_14_2_0, ext = "html")

  fun testCdkNoDataRowNotImported() = doTest(Angular2TestModule.ANGULAR_CDK_14_2_0, ext = "html",
                                             additionalFiles = listOf("${testName}.ts"))

  fun testComponentDecorator() = doTest(Angular2TestModule.ANGULAR_CORE_16_0_0_NEXT_4)

  fun testUnknownDirective() = doTestWithDeps()

  fun testDirectiveInputNoDoc() = doTest()

  fun testDirectiveInOutNoDoc() = doTest()

  fun testDirectiveNoDocInOutDoc() = doTest()

  fun testDirectiveInOutMixedDoc() = doTest()

  fun testDirectiveWithGenerics() = doTest()

  fun testStructuralDirectiveWithGenerics() = doTest(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                                     Angular2TestModule.ANGULAR_COMMON_15_1_5)

  private fun doTestWithDeps() {
    configure(additionalFiles = listOf("deps/list-item.component.ts", "deps/ng_for_of.ts", "deps/ng_if.ts", "deps/dir.ts",
                                       "deps/ng_plural.ts"),
              extension = "html")
    myFixture.checkDocumentationAtCaret()
  }

  private fun doTest(vararg modules: Angular2TestModule, ext: String = "ts", additionalFiles: List<String> = emptyList()) {
    configure(modules = modules, extension = ext, additionalFiles = additionalFiles)
    myFixture.checkDocumentationAtCaret()
  }
}
