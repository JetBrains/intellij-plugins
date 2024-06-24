package org.angular2.codeInsight

import com.intellij.webSymbols.checkDocumentationAtCaret
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2DocumentationTest : Angular2TestCase("documentation", true) {

  fun testTagName() = doTestWithDeps()

  fun testSimpleInput() = doTestWithDeps()

  fun testSimpleInputBinding() = doTestWithDeps()

  fun testSimpleOutputBinding() = doTestWithDeps()

  fun testSimpleBananaBox() = doTestWithDeps()

  // TODO WEB-67260 - fix issues with documentation
  fun _testDirectiveWithMatchingInput() = doTestWithDeps()

  // TODO WEB-67260 - fix issues with typescript.compiler.evaluation.jsTypeDeclaration flag
  fun _testDirectiveWithoutMatchingInput() = doTestWithDeps()

  fun testGlobalAttribute() = doTestWithDeps()

  fun testFieldWithoutDocs() = doTestWithDeps()

  // TODO WEB-67260 - fix issues with typescript.compiler.evaluation.jsTypeDeclaration flag
  fun _testFieldWithDocsPrivate() = doTestWithDeps()

  fun testExtendedEventKey() = doTestWithDeps()

  fun testCdkNoDataRow() = doTest(Angular2TestModule.ANGULAR_CDK_14_2_0, ext = "html")

  fun testCdkNoDataRowNotImported() = doTest(Angular2TestModule.ANGULAR_CDK_14_2_0, ext = "html",
                                             additionalFiles = listOf("${testName}.ts"))

  fun testComponentDecorator() = doTest(Angular2TestModule.ANGULAR_CORE_16_2_8)

  fun testUnknownDirective() = doTestWithDeps()

  fun testDirectiveInputNoDoc() = doTest()

  // TODO WEB-67260 - fix issues with typescript.compiler.evaluation.jsTypeDeclaration flag
  fun _testDirectiveInOutNoDoc() = doTest()

  // TODO WEB-67260 - fix issues with typescript.compiler.evaluation.jsTypeDeclaration flag
  fun _testDirectiveNoDocInOutDoc() = doTest()

  // TODO WEB-67260 - fix issues with typescript.compiler.evaluation.jsTypeDeclaration flag
  fun _testDirectiveInOutMixedDoc() = doTest()

  // TODO WEB-67260 - fix issues with documentation
  fun _testDirectiveWithGenerics() = doTest()

  // TODO WEB-67260 - fix issues with typescript.compiler.evaluation.jsTypeDeclaration flag
  fun _testStructuralDirectiveWithGenerics() = doTest(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                                     Angular2TestModule.ANGULAR_COMMON_15_1_5)

  fun testHostDirectiveMappedInput() = doTest()

  fun testWritableSignal() = doTest(Angular2TestModule.ANGULAR_CORE_16_2_8,
                                    Angular2TestModule.ANGULAR_COMMON_16_2_8)

  // TODO WEB-67260 - fix issues with documentation
  fun _testWritableSignalCall() = doTest(Angular2TestModule.ANGULAR_CORE_16_2_8,
                                        Angular2TestModule.ANGULAR_COMMON_16_2_8)

  fun testSignal() = doTest(Angular2TestModule.ANGULAR_CORE_16_2_8,
                            Angular2TestModule.ANGULAR_COMMON_16_2_8)

  fun testSignalCall() = doTest(Angular2TestModule.ANGULAR_CORE_16_2_8,
                                Angular2TestModule.ANGULAR_COMMON_16_2_8)

  fun testBlock() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                           Angular2TestModule.ANGULAR_COMMON_17_3_0,
                           ext = "html")

  fun testSecondaryBlock() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                                    Angular2TestModule.ANGULAR_COMMON_17_3_0,
                                    ext = "html")

  fun testBlockParameter() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                                    ext = "html")

  fun testForBlockImplicitVariableInLet() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                                                   ext = "html")

  fun testForBlockImplicitVariableInExpr() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                                                    ext = "html")

  fun testDeferBlockPrefetchOn() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0, ext = "html")

  fun testDeferBlockOnTrigger() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0, ext = "html")

  fun testDefaultValueJSDoc() = doTest()

  private fun doTestWithDeps() {
    doConfiguredTest(additionalFiles = listOf("deps/list-item.component.ts", "deps/ng_for_of.ts", "deps/ng_if.ts", "deps/dir.ts",
                                              "deps/ng_plural.ts"),
                     extension = "html") {
      checkDocumentationAtCaret()
    }
  }

  private fun doTest(vararg modules: Angular2TestModule, ext: String = "ts", additionalFiles: List<String> = emptyList()) {
    doConfiguredTest(modules = modules, extension = ext, additionalFiles = additionalFiles) {
      checkDocumentationAtCaret()
    }
  }
}
