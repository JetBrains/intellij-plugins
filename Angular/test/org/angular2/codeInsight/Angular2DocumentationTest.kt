package org.angular2.codeInsight

import com.intellij.javascript.testFramework.web.WebFrameworkTestConfigurator
import com.intellij.webSymbols.testFramework.checkDocumentationAtCaret
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile

class Angular2DocumentationTest : Angular2TestCase("documentation", true) {

  fun testTagName() = doTestWithDeps()

  fun testSimpleInput() = doTestWithDeps()

  fun testSimpleInputBinding() = doTestWithDeps()

  fun testSimpleOutputBinding() = doTestWithDeps()

  fun testSimpleBananaBox() = doTestWithDeps()

  fun testDirectiveWithMatchingInput() = doTestWithDeps(useConfig = true)

  fun testDirectiveWithoutMatchingInput() = doTestWithDeps()

  fun testGlobalAttribute() = doTestWithDeps()

  fun testFieldWithoutDocs() = doTestWithDeps()

  fun testFieldWithDocsPrivate() = doTestWithDeps(useConfig = true)

  fun testExtendedEventKey() = doTestWithDeps()

  fun testCdkNoDataRow() = doTest(Angular2TestModule.ANGULAR_CDK_14_2_0, ext = "html")

  fun testCdkNoDataRowNotImported() = doTest(Angular2TestModule.ANGULAR_CDK_14_2_0, ext = "html",
                                             additionalFiles = listOf("${testName}.ts"))

  fun testComponentDecorator() = doTest(Angular2TestModule.ANGULAR_CORE_16_2_8)

  fun testUnknownDirective() = doTestWithDeps()

  fun testDirectiveInputNoDoc() = doTest()

  fun testDirectiveInOutNoDoc() = doTest(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                         Angular2TestModule.ANGULAR_COMMON_15_1_5)

  fun testDirectiveNoDocInOutDoc() = doTest(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                            Angular2TestModule.ANGULAR_COMMON_15_1_5)

  fun testDirectiveInOutMixedDoc() = doTest(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                            Angular2TestModule.ANGULAR_COMMON_15_1_5)

  fun testDirectiveWithGenerics() = doTest(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                           Angular2TestModule.ANGULAR_COMMON_15_1_5,
                                           configurators = listOf(Angular2TsConfigFile()))

  fun testStructuralDirectiveWithGenerics() = doTest(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                                     Angular2TestModule.ANGULAR_COMMON_15_1_5,
                                                     configurators = listOf(Angular2TsConfigFile()))

  fun testHostDirectiveMappedInput() = doTest()

  fun testWritableSignal() = doTest(Angular2TestModule.ANGULAR_CORE_16_2_8,
                                    Angular2TestModule.ANGULAR_COMMON_16_2_8)

  fun testWritableSignalCall() = doTest(Angular2TestModule.ANGULAR_CORE_16_2_8,
                                        Angular2TestModule.ANGULAR_COMMON_16_2_8,
                                        configurators = listOf(Angular2TsConfigFile()))

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

  fun testDeferBlockHydrate() = doTest(Angular2TestModule.ANGULAR_CORE_19_0_0_NEXT_4, ext = "html")

  fun testDeferBlockHydrateNever() = doTest(Angular2TestModule.ANGULAR_CORE_19_0_0_NEXT_4, ext = "html")

  fun testDefaultValueJSDoc() = doTest()

  private fun doTestWithDeps(useConfig: Boolean = false) {
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_16_2_8,
                     additionalFiles = listOf("deps/list-item.component.ts", "deps/ng_for_of.ts", "deps/ng_if.ts", "deps/dir.ts",
                                              "deps/ng_plural.ts", "deps/module.ts"),
                     configureFile = false,
                     configurators = if (useConfig) listOf(Angular2TsConfigFile()) else emptyList()) {
      myFixture.configureByText("component.ts", """
        import {Component} from "@angular/core";
        import {Module} from "./deps/module"

        @Component({
         standalone: true,
         templateUrl: "./$testName.html",
         imports: [Module]
        })
        class MyComponent {
        }
      """.trimIndent())
      myFixture.configureByFile("$testName.html")
      checkDocumentationAtCaret()
    }
  }

  private fun doTest(
    vararg modules: Angular2TestModule,
    ext: String = "ts",
    additionalFiles: List<String> = emptyList(),
    configurators: List<WebFrameworkTestConfigurator> = emptyList(),
  ) {
    doConfiguredTest(modules = modules, extension = ext, additionalFiles = additionalFiles, configurators = configurators) {
      checkDocumentationAtCaret()
    }
  }
}
