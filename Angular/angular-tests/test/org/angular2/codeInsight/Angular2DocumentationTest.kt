package org.angular2.codeInsight

import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.polySymbols.testFramework.checkDocumentationAtCaret
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.ANGULAR_CORE_20_1_4
import org.angular2.Angular2TestModule.NGRX_SIGNALS_20_1_0
import org.angular2.Angular2TestModule.RXJS_7_8_1
import org.angular2.Angular2TsConfigFile
import org.angular2.TestTsNode
import org.junit.Test

@TestTsNode
//@TestTsGoProxy
class Angular2DocumentationTest : Angular2TestCase("documentation") {

  @Test
  fun testTagName() = doTestWithDeps()

  @Test
  fun testSimpleInput() = doTestWithDeps()

  @Test
  fun testSimpleInputBinding() = doTestWithDeps()

  @Test
  fun testSimpleOutputBinding() = doTestWithDeps()

  @Test
  fun testSimpleBananaBox() = doTestWithDeps()

  @Test
  fun testDirectiveWithMatchingInput() = doTestWithDeps(useConfig = true)

  @Test
  fun testDirectiveWithoutMatchingInput() = doTestWithDeps()

  @Test
  fun testGlobalAttribute() = doTestWithDeps()

  @Test
  fun testFieldWithoutDocs() = doTestWithDeps()

  @Test
  fun testFieldWithDocsPrivate() = doTestWithDeps(useConfig = true)

  @Test
  fun testExtendedEventKey() = doTestWithDeps()

  @Test
  fun testCdkNoDataRow() = doTest(Angular2TestModule.ANGULAR_CDK_14_2_0, ext = "html")

  @Test
  fun testCdkNoDataRowNotImported() = doTest(Angular2TestModule.ANGULAR_CDK_14_2_0, ext = "html",
                                             additionalFiles = listOf("${testName}.ts"))

  @Test
  fun testComponentDecorator() = doTest(Angular2TestModule.ANGULAR_CORE_16_2_8)

  @Test
  fun testUnknownDirective() = doTestWithDeps()

  @Test
  fun testDirectiveInputNoDoc() = doTest()

  @Test
  fun testDirectiveInOutNoDoc() = doTest(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                         Angular2TestModule.ANGULAR_COMMON_15_1_5)

  @Test
  fun testDirectiveNoDocInOutDoc() = doTest(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                            Angular2TestModule.ANGULAR_COMMON_15_1_5)

  @Test
  fun testDirectiveInOutMixedDoc() = doTest(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                            Angular2TestModule.ANGULAR_COMMON_15_1_5)

  @Test
  fun testDirectiveWithGenerics() = doTest(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                           Angular2TestModule.ANGULAR_COMMON_15_1_5,
                                           configurators = listOf(Angular2TsConfigFile()))

  @Test
  fun testStructuralDirectiveWithGenerics() = doTest(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                                     Angular2TestModule.ANGULAR_COMMON_15_1_5,
                                                     configurators = listOf(Angular2TsConfigFile()))

  @Test
  fun testHostDirectiveMappedInput() = doTest()

  @Test
  fun testWritableSignal() = doTest(Angular2TestModule.ANGULAR_CORE_16_2_8,
                                    Angular2TestModule.ANGULAR_COMMON_16_2_8)

  @Test
  fun testWritableSignalCall() = doTest(Angular2TestModule.ANGULAR_CORE_16_2_8,
                                        Angular2TestModule.ANGULAR_COMMON_16_2_8,
                                        configurators = listOf(Angular2TsConfigFile()))

  @Test
  fun testSignal() = doTest(Angular2TestModule.ANGULAR_CORE_16_2_8,
                            Angular2TestModule.ANGULAR_COMMON_16_2_8)

  @Test
  fun testSignalCall() = doTest(Angular2TestModule.ANGULAR_CORE_16_2_8,
                                Angular2TestModule.ANGULAR_COMMON_16_2_8)

  @Test
  fun testBlock() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                           Angular2TestModule.ANGULAR_COMMON_17_3_0,
                           ext = "html")

  @Test
  fun testSecondaryBlock() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                                    Angular2TestModule.ANGULAR_COMMON_17_3_0,
                                    ext = "html")

  @Test
  fun testBlockParameter() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                                    ext = "html")

  @Test
  fun testForBlockImplicitVariableInLet() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                                                   ext = "html")

  @Test
  fun testForBlockImplicitVariableInExpr() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                                                    ext = "html")

  @Test
  fun testDeferBlockPrefetchOn() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0, ext = "html")

  @Test
  fun testDeferBlockOnTrigger() = doTest(Angular2TestModule.ANGULAR_CORE_17_3_0, ext = "html")

  @Test
  fun testDeferBlockHydrate() = doTest(Angular2TestModule.ANGULAR_CORE_19_2_0, ext = "html")

  @Test
  fun testDeferBlockHydrateNever() = doTest(Angular2TestModule.ANGULAR_CORE_19_2_0, ext = "html")

  @Test
  fun testDefaultValueJSDoc() = doTest()

  @Test
  fun testUnknownNgClass() = doTest()

  @Test
  fun testSignalStore() = doTest(ANGULAR_CORE_20_1_4, NGRX_SIGNALS_20_1_0,
                                 configurators = listOf(Angular2TsConfigFile()))

  @Test
  fun testAriaProperty() = doTest(ANGULAR_CORE_20_1_4, ext = "html")

  @Test
  fun testAriaPropertyAlias() = doTest(ANGULAR_CORE_20_1_4, ext = "html")

  @Test
  fun testComponentLifecycleHook() = doTest(ANGULAR_CORE_20_1_4, ext = "ts")

  @Test
  fun testIonicLifecycleHook() = doTest(ANGULAR_CORE_20_1_4, Angular2TestModule.IONIC_ANGULAR_8_4_3, ext = "ts")

  @Test
  fun testRxjsOperator() = doTest(ANGULAR_CORE_20_1_4, RXJS_7_8_1, ext = "ts")

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
      checkDocumentationAtCaret(fileSuffix = calculateFileSuffix())
    }
  }

  private fun doTest(
    vararg modules: Angular2TestModule,
    ext: String = "ts",
    additionalFiles: List<String> = emptyList(),
    configurators: List<PolySymbolsTestConfigurator> = emptyList(),
  ) {
    doConfiguredTest(modules = modules, extension = ext, additionalFiles = additionalFiles, configurators = configurators) {
      checkDocumentationAtCaret(fileSuffix = calculateFileSuffix())
    }
  }

  private fun CodeInsightTestFixture.calculateFileSuffix(): String {
    //if (serviceKind == TypeScriptServiceKind.TsGoProxy) {
    //  val expectedFile = InjectedLanguageManager.getInstance(project).getTopLevelFile(file)
    //                       .virtualFile.nameWithoutExtension + ".expected.tsgo.html"
    //  if (File("$testDataPath/$expectedFile").exists())
    //    return ".expected.tsgo"
    //}
    return ".expected"
  }
}
