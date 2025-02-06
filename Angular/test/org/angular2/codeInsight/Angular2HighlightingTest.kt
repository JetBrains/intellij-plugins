// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import junit.framework.TestCase
import org.angular2.*
import org.angular2.Angular2TestModule.*
import org.angular2.codeInsight.inspections.Angular2ExpressionTypesInspectionTest
import java.io.File
import java.io.IOException

class Angular2HighlightingTest : Angular2TestCase("highlighting", true) {

  fun testSvgTags() = checkHighlighting(ANGULAR_COMMON_16_2_8, extension = "ts")

  fun testDeprecated() = checkHighlighting(ANGULAR_CORE_15_1_5, dir = true)

  fun testDeprecatedInline() = checkHighlighting(checkInjections = true, extension = "ts")

  fun testNgAcceptInputType() = checkHighlighting(extension = "ts")

  fun testNestedComponentClasses() = checkHighlighting(dir = true)

  fun testGlobalThis() = checkHighlighting(dir = true)

  fun testComplexGenerics() = checkHighlighting(dir = true)

  fun testUnknownTagsAttributesInlineTemplate() = checkHighlighting(extension = "ts")

  fun testStyleUnitLength() = checkHighlighting()

  fun testMatSortHeader() = checkHighlighting(ANGULAR_MATERIAL_14_2_5_MIXED)

  fun testImgSrcWithNg15() = checkHighlighting(ANGULAR_CORE_15_1_5, ANGULAR_COMMON_15_1_5)

  fun testNoRequiredBindingsWithoutModuleScope() =
    checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8)

  fun testNgAcceptInputTypeOverride() = checkHighlighting(extension = "ts")

  /**
   * @see Angular2ExpressionTypesInspectionTest.testNullChecks
   * @see Angular2ExpressionTypesInspectionTest.testNullChecksInline
   */
  fun testTypeMismatchErrorWithOptionalInputs() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8,
                                                                    dir = true, extension = "ts", strictTemplates = true)

  fun testHostDirectives() = checkHighlighting(dir = true)

  fun testAnimationCallbacks() = checkHighlighting(dir = true)

  // The shim doesn't work with TCB
  fun _testElementShims() = checkHighlighting(dir = true, strictTemplates = true)

  fun testCustomUserEvents() = checkHighlighting(dir = true)

  fun testFxLayout() = withTypeScriptServerService(TypeScriptServerServiceImpl::class) {
    checkHighlighting(ANGULAR_CORE_9_1_1_MIXED, ANGULAR_FLEX_LAYOUT_13_0_0)
  }

  fun testHtmlAttributes() = checkHighlighting()

  fun testCdkDirectives() = checkHighlighting(ANGULAR_CDK_14_2_0, dir = true)

  fun testCustomDataAttributes() = checkHighlighting(dir = true)

  fun testI18nAttr() = checkHighlighting(ANGULAR_MATERIAL_8_2_3_MIXED)

  fun testNgNonBindable() = checkHighlighting()

  fun testIonicAttributes() = checkHighlighting(IONIC_ANGULAR_4_1_1)

  /**
   * Tests an older version of library
   *
   * @see Angular2ExpressionTypesInspectionTest.testNgrxLetContextGuard
   */
  fun testNgrxLetAsContextGuard() = checkHighlighting(ANGULAR_CORE_16_2_8, RXJS_7_8_1, dir = true, extension = "ts", strictTemplates = true)

  // TODO WEB-67260 - improve error highlighting
  fun testRequiredInputs() = checkHighlighting(extension = "ts")

  fun testStaticAttributes() = checkHighlighting(dir = true)

  fun testSelfClosedTags() = checkHighlighting(dir = true)

  fun testTrUnderTemplate() = checkHighlighting(ANGULAR_CDK_14_2_0, dir = true)

  fun testDivUnderButton() = checkHighlighting(ANGULAR_MATERIAL_16_2_8, dir = true)

  // TODO WEB-67260 - improve error highlighting
  fun _testReadOnlyTemplateVariables() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, extension = "ts")

  fun testDirectiveWithStandardAttrSelector() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8,
                                                                  strictTemplates = true, extension = "ts")

  fun testInputsWithTransform() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, strictTemplates = true, dir = true)

  fun testOneTimeBindingOfPrimitives() = checkHighlighting(dir = true, configureFileName = "one_time_binding.html")

  fun testOneTimeBindingOfPrimitivesStrict() = checkHighlighting(dir = true, configureFileName = "one_time_binding.html",
                                                                 strictTemplates = true)

  fun testNgCspNonceNg15() = checkHighlighting(ANGULAR_CORE_15_1_5)

  fun testNgCspNonceNg16() = checkHighlighting(ANGULAR_CORE_16_2_8)

  fun testNgSkipHydrationNg15() = checkHighlighting(ANGULAR_CORE_15_1_5)

  fun testNgSkipHydrationNg16() = checkHighlighting(ANGULAR_CORE_16_2_8)

  fun testUndefinedInterpolationBinding() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_ROUTER_16_2_8, dir = true,
                                                              configureFileName = "hero-search.component.html")

  fun testSubjectGenericInference() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, RXJS_7_8_1,
                                                        extension = "ts", strictTemplates = true)

  fun testComplexFormControls() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8,
                                                    extension = "ts", strictTemplates = true)

  fun testSignalsColors() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8,
                                              extension = "ts", strictTemplates = true, checkSymbolNames = true)

  fun testSignalsColorsHtml() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, dir = true,
                                                  configureFileName = "signalsColors.html", checkSymbolNames = true)

  fun testTemplateColorsHtml() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8, dir = true,
                                                   configureFileName = "colors.html", checkSymbolNames = true)

  // TODO WEB-67260 - fix issues with RainbowColors
  fun _testRainbowColorsHtml() = doConfiguredTest(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8, dir = true,
                                                  configureFile = false) {
    myFixture.testRainbow("colors.html", FileUtil.loadFile(File("$testDataPath/$testName/colors.html")), true, true)
  }

  fun testBlockDefer() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  fun testBlockDeferHydrateNg18() {
    checkHighlighting(ANGULAR_CORE_18_2_1, extension = "ts")
  }

  fun testBlockDeferHydrateNg19() {
    checkHighlighting(ANGULAR_CORE_19_0_0_NEXT_4, extension = "ts")
  }

  fun testBlockFor() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  fun testBlockForMultiVars() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  fun testBlockIf() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  fun testBlockSwitch() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  fun testInterpolationStrictMode() = checkHighlighting(ANGULAR_CORE_16_2_8, strictTemplates = true, extension = "ts")

  fun testOneTimeAttributesWithoutValueInStrictMode() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_MATERIAL_16_2_8,
                                                                          strictTemplates = true, extension = "ts")

  fun testNgSrcAttribute() = checkHighlighting(ANGULAR_CORE_17_3_0, ANGULAR_COMMON_17_3_0,
                                               strictTemplates = true, extension = "ts", dir = true)

  fun testIfBlockAsVarType() = checkHighlighting(ANGULAR_CORE_17_3_0, ANGULAR_COMMON_17_3_0,
                                                 strictTemplates = true, extension = "ts")

  fun testForBlockSemanticOfHighlighting() = checkHighlighting(ANGULAR_CORE_17_3_0,
                                                               strictTemplates = true, extension = "ts", checkSymbolNames = true)

  fun testForBlockVarType() = checkHighlighting(ANGULAR_CORE_17_3_0,
                                                strictTemplates = true, extension = "ts")

  fun testForBlockIterableType() = checkHighlighting(ANGULAR_CORE_17_3_0, ANGULAR_COMMON_17_3_0, RXJS_7_8_1,
                                                     strictTemplates = true, extension = "ts")

  fun testDeferBlockSemanticHighlighting() = checkHighlighting(ANGULAR_CORE_17_3_0,
                                                               strictTemplates = true, extension = "html", checkSymbolNames = true)

  // TODO WEB-67260 - improve error highlighting
  fun testInputSignals() = checkHighlighting(ANGULAR_CORE_17_3_0, configureFileName = "test.html",
                                             strictTemplates = true, dir = true)

  fun testSvgNoBlocksSyntax() = checkHighlighting(ANGULAR_CORE_16_2_8, extension = "svg")

  fun testSvgWithBlocksSyntax() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "svg")

  fun testSvgWithLetSyntax() = checkHighlighting(ANGULAR_CORE_18_2_1, extension = "svg")

  fun testAnimationTriggerNoAttributeValue() = checkHighlighting()

  fun testModuleWithExportDefault() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, RXJS_7_8_1,
                                                        configureFileName = "comp-b.component.ts", dir = true)

  fun testNgAcceptInputOverAlias() = checkHighlighting(ANGULAR_CORE_17_3_0, ANGULAR_CDK_17_1_0_RC_0,
                                                       strictTemplates = true, extension = "ts")

  fun testTupleAssignment() = checkHighlighting(ANGULAR_CORE_16_2_8,
                                                strictTemplates = true, extension = "ts")

  fun testNgNativeValidate() = checkHighlighting(ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8,
                                                 extension = "ts")

  fun testStrictNullChecks() = checkHighlighting(dir = true, configureFileName = "src/check.ts",
                                                 configurators = listOf())

  fun testSetterWithGenericParameter() = checkHighlighting(ANGULAR_CORE_16_2_8,
                                                           strictTemplates = true, extension = "ts")

  fun testForwardRef() = checkHighlighting(ANGULAR_CORE_16_2_8,
                                           strictTemplates = true, extension = "ts")

  fun testOutputSignals() = checkHighlighting(ANGULAR_CORE_17_3_0, RXJS_7_8_1,
                                              strictTemplates = true, extension = "ts")

  fun testModelSignals() = checkHighlighting(ANGULAR_CORE_17_3_0,
                                             strictTemplates = true, extension = "ts")

  // TODO WEB-67260 - improve error highlighting
  fun testOptionalTemplateRef() = checkHighlighting(ANGULAR_CORE_16_2_8, extension = "ts")

  fun testStructuralDirectiveWithNgTemplateSelector() = checkHighlighting(ANGULAR_CORE_16_2_8, extension = "ts")

  fun testStdTagAttributeMappings() = checkHighlighting(ANGULAR_CORE_16_2_8, extension = "ts")

  fun testSignalInputOutputModelNotUnused() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  fun testStandalonePseudoModules() = checkHighlighting(configureFileName = "check.ts", dir = true)

  fun testNgIfOverObjectWithGenericArguments() = checkHighlighting(ANGULAR_FORMS_16_2_8, ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8,
                                                                   extension = "ts")

  fun testTemplateTagAttributes() = checkHighlighting()

  fun testPipeGenericTypeNarrowing() = checkHighlighting(ANGULAR_CORE_16_2_8, extension = "ts")

  fun testPipeOverloadWithUndefined() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, RXJS_7_8_1,
                                                          dir = true, configureFileName = "apps/app.component.html",
                                                          configurators = listOf())

  fun testLetDeclaration() = checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  fun testLetDeclaration2() = checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  fun testCrLfComponentFile() =
    doConfiguredTest(ANGULAR_CORE_17_3_0, ANGULAR_COMMON_17_3_0, extension = "ts", configurators = listOf(Angular2TsConfigFile())) {
      checkHighlightingWithCrLfEnsured()
    }

  fun testTsconfigPriority() =
    checkHighlighting(ANGULAR_CORE_17_3_0, extension = "html", dir = true, configureFileName = "src/component.html",
                      configurators = listOf())

  fun testDirectiveInputInNgTemplate() =
    checkHighlighting(extension = "ts")

  fun testTuiLet() =
    checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  fun testTypeofNg18() =
    checkHighlighting(ANGULAR_CORE_18_2_1, dir = true, configureFileName = "typeof.html")

  fun testTypeofNg19() =
    checkHighlighting(ANGULAR_CORE_19_0_0_NEXT_4, dir = true, configureFileName = "typeof.html")

  fun testConfigWithMapping() =
    checkHighlighting(ANGULAR_CORE_18_2_1, dir = true, configureFileName = "projects/frontend/src/app/app.component.html",
                      configurators = listOf(Angular2TsExpectedConfigFiles("projects/frontend/tsconfig.app.json")))

  fun testComponentWithParenthesis() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  fun testHostBindings() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  fun testHostBindingsSyntax() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts", checkSymbolNames = true)

  fun testDirectiveSelectorsSyntax() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts", checkSymbolNames = true)

  fun testBindingsSyntax() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts", checkSymbolNames = true)

  fun testHostDirectivesSyntax() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts", checkSymbolNames = true)

  fun testEs6ObjectInitializer() =
    checkHighlighting(ANGULAR_CORE_18_2_1, extension = "ts")

  fun testUnusedTemplateVariable() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  // TODO - JSUnusedGlobalSymbolsPass doesn't support injections
  fun _testViewChildrenDecorator() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  fun testViewChildrenDecoratorHtml() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, dir = true)

  fun testViewChildrenDecoratorSyntax() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts", checkSymbolNames = true)

  fun testViewChildrenSignal() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, dir = true)

  fun testViewChildrenSignalSyntax() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts", checkSymbolNames = true)

  fun testTemplateBindingsNgFor() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
  }

  private fun checkHighlighting(
    vararg modules: Angular2TestModule,
    dir: Boolean = false,
    checkInjections: Boolean = false,
    strictTemplates: Boolean = false,
    extension: String = "html",
    configureFileName: String = "$testName.$extension",
    checkSymbolNames: Boolean = false,
    checkInformation: Boolean = checkSymbolNames,
  ) {
    checkHighlighting(
      *modules, dir = dir, extension = extension, configureFileName = configureFileName,
      configurators = listOf(Angular2TsConfigFile(strictTemplates = strictTemplates)),
      checkInjections = checkInjections,
      checkSymbolNames = checkSymbolNames,
      checkInformation = checkInformation,
    )
  }

  override fun adjustModules(modules: Array<out WebFrameworkTestModule>): Array<out WebFrameworkTestModule> {
    val result = mutableSetOf(*modules)

    if (TS_LIB !in result) {
      result.add(TS_LIB)
    }

    if (result.flatMap { it.packageNames }.none { it == "@angular/core" }) {
      result.add(ANGULAR_CORE_16_2_8)
    }

    return result.toTypedArray()
  }

  private fun checkHighlightingWithCrLfEnsured() {
    val virtualFile = myFixture.file.virtualFile
    val data = ExpectedHighlightingData(myFixture.getEditor().getDocument(), true, true, false, true)
    data.init()
    WriteCommandAction.writeCommandAction(project).withName("Change line separators").run<RuntimeException> {
      try {
        FileDocumentManager.getInstance().saveAllDocuments()
        LoadTextUtil.changeLineSeparators(project, virtualFile, "\r\n", virtualFile)
        FileDocumentManager.getInstance().reloadFromDisk(myFixture.editor.document)
      }
      catch (e: IOException) {
        throw RuntimeException(e)
      }
    }
    TestCase.assertEquals("\r\n", FileDocumentManager.getInstance().getLineSeparator(virtualFile, project))
    (myFixture as CodeInsightTestFixtureImpl).collectAndCheckHighlighting(data)
    TestCase.assertEquals("\r\n", FileDocumentManager.getInstance().getLineSeparator(virtualFile, project))
  }

}