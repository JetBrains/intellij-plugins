// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import junit.framework.TestCase
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.ANGULAR_CDK_14_2_0
import org.angular2.Angular2TestModule.ANGULAR_CDK_17_1_0_RC_0
import org.angular2.Angular2TestModule.ANGULAR_COMMON_15_1_5
import org.angular2.Angular2TestModule.ANGULAR_COMMON_16_2_8
import org.angular2.Angular2TestModule.ANGULAR_COMMON_17_3_0
import org.angular2.Angular2TestModule.ANGULAR_COMMON_18_2_1
import org.angular2.Angular2TestModule.ANGULAR_CORE_13_3_5
import org.angular2.Angular2TestModule.ANGULAR_CORE_15_1_5
import org.angular2.Angular2TestModule.ANGULAR_CORE_16_2_8
import org.angular2.Angular2TestModule.ANGULAR_CORE_17_3_0
import org.angular2.Angular2TestModule.ANGULAR_CORE_18_2_1
import org.angular2.Angular2TestModule.ANGULAR_CORE_19_2_0
import org.angular2.Angular2TestModule.ANGULAR_CORE_20_0_0_NEXT_3
import org.angular2.Angular2TestModule.ANGULAR_CORE_20_1_4
import org.angular2.Angular2TestModule.ANGULAR_CORE_20_2_2
import org.angular2.Angular2TestModule.ANGULAR_CORE_21_0_9
import org.angular2.Angular2TestModule.ANGULAR_CORE_21_1_3
import org.angular2.Angular2TestModule.ANGULAR_CORE_21_2_0
import org.angular2.Angular2TestModule.ANGULAR_CORE_9_1_1_MIXED
import org.angular2.Angular2TestModule.ANGULAR_FLEX_LAYOUT_13_0_0
import org.angular2.Angular2TestModule.ANGULAR_FORMS_16_2_8
import org.angular2.Angular2TestModule.ANGULAR_MATERIAL_14_2_5_MIXED
import org.angular2.Angular2TestModule.ANGULAR_MATERIAL_16_2_8
import org.angular2.Angular2TestModule.ANGULAR_MATERIAL_8_2_3_MIXED
import org.angular2.Angular2TestModule.ANGULAR_ROUTER_16_2_8
import org.angular2.Angular2TestModule.IONIC_ANGULAR_4_1_1
import org.angular2.Angular2TestModule.IONIC_ANGULAR_8_4_3
import org.angular2.Angular2TestModule.IONIC_CORE_8_4_3
import org.angular2.Angular2TestModule.NGRX_EFFECTS_21_0_1
import org.angular2.Angular2TestModule.NGRX_SIGNALS_20_1_0
import org.angular2.Angular2TestModule.RXJS_7_8_1
import org.angular2.Angular2TestModule.TS_LIB
import org.angular2.Angular2TsConfigFile
import org.angular2.Angular2TsExpectedConfigFiles
import org.angular2.SkipTsGoProxy
import org.angular2.TestTsGoProxy
import org.angular2.TestTsNode
import org.angular2.codeInsight.inspections.Angular2ExpressionTypesInspectionTest
import org.junit.Test
import java.io.File
import java.io.IOException

@TestTsNode
@TestTsGoProxy
class Angular2HighlightingTest : Angular2TestCase("highlighting") {

  @Test
  fun testSvgTags() = checkHighlighting(ANGULAR_COMMON_16_2_8, extension = "ts")

  @Test
  fun testDeprecated() = checkHighlighting(ANGULAR_CORE_15_1_5, dir = true)

  @Test
  fun testDeprecatedInline() = checkHighlighting(checkInjections = true, extension = "ts")

  @Test
  fun testNgAcceptInputType() = checkHighlighting(extension = "ts")

  @Test
  fun testNestedComponentClasses() = checkHighlighting(dir = true)

  @Test
  fun testGlobalThis() = checkHighlighting(dir = true)

  @Test
  fun testGlobalThisNg19() = checkHighlighting(ANGULAR_CORE_19_2_0, dir = true, configureFileName = "globalThis.html")

  @Test
  fun testComplexGenerics() = checkHighlighting(dir = true)

  @Test
  fun testUnknownTagsAttributesInlineTemplate() = checkHighlighting(extension = "ts")

  @Test
  fun testStyleUnit() = checkHighlighting()

  @Test
  fun testMatSortHeader() = checkHighlighting(ANGULAR_MATERIAL_14_2_5_MIXED)

  @Test
  fun testImgSrcWithNg15() = checkHighlighting(ANGULAR_CORE_15_1_5, ANGULAR_COMMON_15_1_5)

  @Test
  fun testNoRequiredBindingsWithoutModuleScope() =
    checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8)

  @Test
  fun testNgAcceptInputTypeOverride() = checkHighlighting(extension = "ts")

  /**
   * @see Angular2ExpressionTypesInspectionTest.testNullChecks
   * @see Angular2ExpressionTypesInspectionTest.testNullChecksInline
   */
  // TODO WEB-78127 Hanging tests in ijplatform_master_Idea_Tests_JavascriptTests_7
  //fun testTypeMismatchErrorWithOptionalInputs() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8,
  //                                                                  dir = true, extension = "ts", strictTemplates = true)

  @Test
  fun testHostDirectives() = checkHighlighting(dir = true)

  @Test
  fun testAnimationCallbacks() = checkHighlighting(dir = true)

  // The shim doesn't work with TCB
  fun _testElementShims() = checkHighlighting(dir = true, strictTemplates = true)

  @Test
  fun testCustomUserEvents() = checkHighlighting(dir = true)

  @Test
  fun testFxLayout() = checkHighlighting(ANGULAR_CORE_9_1_1_MIXED, ANGULAR_FLEX_LAYOUT_13_0_0)

  @Test
  fun testHtmlAttributes() = checkHighlighting()

  @Test
  fun testCdkDirectives() = checkHighlighting(ANGULAR_CDK_14_2_0, dir = true)

  @Test
  fun testCustomDataAttributes() = checkHighlighting(dir = true)

  @Test
  fun testI18nAttr() = checkHighlighting(ANGULAR_MATERIAL_8_2_3_MIXED)

  @Test
  fun testNgNonBindable() = checkHighlighting()

  @Test
  fun testIonicAttributes() = checkHighlighting(IONIC_ANGULAR_4_1_1)

  /**
   * Tests an older version of library
   *
   * @see Angular2ExpressionTypesInspectionTest.testNgrxLetContextGuard
   */
  @Test
  fun testNgrxLetAsContextGuard() = checkHighlighting(ANGULAR_CORE_16_2_8, RXJS_7_8_1, dir = true, extension = "ts", strictTemplates = true)

  // TODO WEB-67260 - improve error highlighting
  @Test
  fun testRequiredInputs() = checkHighlighting(extension = "ts")

  @Test
  fun testStaticAttributes() = checkHighlighting(dir = true)

  @Test
  fun testSelfClosedTags() = checkHighlighting(dir = true)

  @Test
  fun testTrUnderTemplate() = checkHighlighting(ANGULAR_CDK_14_2_0, dir = true)

  @Test
  fun testDivUnderButton() = checkHighlighting(ANGULAR_MATERIAL_16_2_8, dir = true)

  // TODO WEB-67260 - improve error highlighting
  fun _testReadOnlyTemplateVariables() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, extension = "ts")

  @Test
  fun testDirectiveWithStandardAttrSelector() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8,
                                                                  strictTemplates = true, extension = "ts")

  @Test
  fun testInputsWithTransform() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, strictTemplates = true, dir = true)

  @Test
  fun testOneTimeBindingOfPrimitives() = checkHighlighting(dir = true, configureFileName = "one_time_binding.html")

  @Test
  fun testOneTimeBindingOfPrimitivesStrict() = checkHighlighting(dir = true, configureFileName = "one_time_binding.html",
                                                                 strictTemplates = true)

  @Test
  fun testNgCspNonceNg15() = checkHighlighting(ANGULAR_CORE_15_1_5)

  @Test
  fun testNgCspNonceNg16() = checkHighlighting(ANGULAR_CORE_16_2_8)

  @Test
  fun testNgSkipHydrationNg15() = checkHighlighting(ANGULAR_CORE_15_1_5)

  @Test
  fun testNgSkipHydrationNg16() = checkHighlighting(ANGULAR_CORE_16_2_8)

  @Test
  fun testUndefinedInterpolationBinding() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_ROUTER_16_2_8, dir = true,
                                                              configureFileName = "hero-search.component.html")

  @Test
  fun testSubjectGenericInference() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, RXJS_7_8_1,
                                                        extension = "ts", strictTemplates = true)

  @Test
  fun testComplexFormControls() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8,
                                                    extension = "ts", strictTemplates = true)

  @Test
  fun testSignalsColors() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8,
                                              extension = "ts", strictTemplates = true, checkSymbolNames = true)

  @Test
  fun testSignalsColorsHtml() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, dir = true,
                                                  configureFileName = "signalsColors.html", checkSymbolNames = true)

  // TODO WEB-78127 Hanging tests in ijplatform_master_Idea_Tests_JavascriptTests_7
  //fun testTemplateColorsHtml() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8, dir = true,
  //                                                 configureFileName = "colors.html", checkSymbolNames = true)

  // TODO WEB-67260 - fix issues with RainbowColors
  fun _testRainbowColorsHtml() = doConfiguredTest(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8, dir = true,
                                                  configureFile = false) {
    myFixture.testRainbow("colors.html", FileUtil.loadFile(File("$testDataPath/$testName/colors.html")), true, true)
  }

  @Test
  @SkipTsGoProxy // missing unresolved symbol
  fun testBlockDefer() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testBlockDeferHydrateNg18() {
    checkHighlighting(ANGULAR_CORE_18_2_1, extension = "ts")
  }

  @Test
  fun testBlockDeferHydrateNg19() {
    checkHighlighting(ANGULAR_CORE_19_2_0, extension = "ts")
  }

  @Test
  fun testBlockFor() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testBlockForMultiVars() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testBlockIf() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testBlockIfNg20_2() = checkHighlighting(ANGULAR_CORE_20_2_2, extension = "ts")

  @Test
  fun testBlockSwitch() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testBlockSwitchNg21_1() = checkHighlighting(ANGULAR_CORE_21_1_3, extension = "ts")

  @Test
  fun testInterpolationStrictMode() = checkHighlighting(ANGULAR_CORE_16_2_8, strictTemplates = true, extension = "ts")

  @Test
  fun testOneTimeAttributesWithoutValueInStrictMode() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_MATERIAL_16_2_8,
                                                                          strictTemplates = true, extension = "ts")

  @Test
  fun testNgSrcAttribute() = checkHighlighting(ANGULAR_CORE_17_3_0, ANGULAR_COMMON_17_3_0,
                                               strictTemplates = true, extension = "ts", dir = true)

  @Test
  fun testIfBlockAsVarType() = checkHighlighting(ANGULAR_CORE_17_3_0, ANGULAR_COMMON_17_3_0,
                                                 strictTemplates = true, extension = "ts")

  @Test
  fun testForBlockSemanticOfHighlighting() = checkHighlighting(ANGULAR_CORE_17_3_0,
                                                               strictTemplates = true, extension = "ts", checkSymbolNames = true)

  @Test
  fun testForBlockVarType() = checkHighlighting(ANGULAR_CORE_17_3_0,
                                                strictTemplates = true, extension = "ts")

  @Test
  fun testForBlockIterableType() = checkHighlighting(ANGULAR_CORE_17_3_0, ANGULAR_COMMON_17_3_0, RXJS_7_8_1,
                                                     strictTemplates = true, extension = "ts")

  @Test
  fun testForBlockAliasVars() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  @Test
  fun testDeferBlockSemanticHighlighting() = checkHighlighting(ANGULAR_CORE_17_3_0,
                                                               strictTemplates = true, extension = "html", checkSymbolNames = true)

  // TODO WEB-67260 - improve error highlighting
  @Test
  fun testInputSignals() = checkHighlighting(ANGULAR_CORE_17_3_0, configureFileName = "test.html",
                                             strictTemplates = true, dir = true)

  @Test
  fun testSvgNoBlocksSyntax() = checkHighlighting(ANGULAR_CORE_16_2_8, extension = "svg")

  @Test
  fun testSvgWithBlocksSyntax() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "svg")

  @Test
  fun testSvgWithLetSyntax() = checkHighlighting(ANGULAR_CORE_18_2_1, extension = "svg")

  @Test
  fun testAnimationTriggerNoAttributeValue() = checkHighlighting()

  @Test
  fun testModuleWithExportDefault() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, RXJS_7_8_1,
                                                        configureFileName = "comp-b.component.ts", dir = true)

  @Test
  fun testNgAcceptInputOverAlias() = checkHighlighting(ANGULAR_CORE_17_3_0, ANGULAR_CDK_17_1_0_RC_0,
                                                       strictTemplates = true, extension = "ts")

  @Test
  fun testTupleAssignment() = checkHighlighting(ANGULAR_CORE_16_2_8,
                                                strictTemplates = true, extension = "ts")

  @Test
  fun testNgNativeValidate() = checkHighlighting(ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8,
                                                 extension = "ts")

  @Test
  fun testStrictNullChecks() = checkHighlighting(dir = true, configureFileName = "src/check.ts",
                                                 configurators = listOf())

  @Test
  fun testSetterWithGenericParameter() = checkHighlighting(ANGULAR_CORE_16_2_8,
                                                           strictTemplates = true, extension = "ts")

  @Test
  fun testForwardRef() = checkHighlighting(ANGULAR_CORE_16_2_8,
                                           strictTemplates = true, extension = "ts")

  @Test
  fun testOutputSignals() = checkHighlighting(ANGULAR_CORE_17_3_0, RXJS_7_8_1,
                                              strictTemplates = true, extension = "ts")

  @Test
  fun testModelSignals() = checkHighlighting(ANGULAR_CORE_17_3_0,
                                             strictTemplates = true, extension = "ts")

  // TODO WEB-67260 - improve error highlighting
  @Test
  fun testOptionalTemplateRef() = checkHighlighting(ANGULAR_CORE_16_2_8, extension = "ts")

  @Test
  fun testStructuralDirectiveWithNgTemplateSelector() = checkHighlighting(ANGULAR_CORE_16_2_8, extension = "ts")

  @Test
  fun testStdTagAttributeMappings() = checkHighlighting(ANGULAR_CORE_16_2_8, extension = "ts")

  @Test
  fun testSignalInputOutputModelNotUnused() = checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testStandalonePseudoModules() = checkHighlighting(configureFileName = "check.ts", dir = true)

  @Test
  fun testNgIfOverObjectWithGenericArguments() = checkHighlighting(ANGULAR_FORMS_16_2_8, ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8,
                                                                   extension = "ts")

  @Test
  fun testTemplateTagAttributes() = checkHighlighting()

  @Test
  fun testPipeGenericTypeNarrowing() = checkHighlighting(ANGULAR_CORE_16_2_8, extension = "ts")

  @Test
  fun testPipeOverloadWithUndefined() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, RXJS_7_8_1,
                                                          dir = true, configureFileName = "apps/app.component.html",
                                                          configurators = listOf())

  @Test
  fun testLetDeclaration() = checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  @Test
  fun testLetDeclaration2() = checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  @Test
  fun testCrLfComponentFile() =
    doConfiguredTest(ANGULAR_CORE_17_3_0, ANGULAR_COMMON_17_3_0, extension = "ts", configurators = listOf(Angular2TsConfigFile())) {
      checkHighlightingWithCrLfEnsured()
    }

  @Test
  fun testCrLfComponentFileDirectives() =
    doConfiguredTest(ANGULAR_CORE_19_2_0, extension = "ts", configurators = listOf(Angular2TsConfigFile())) {
      checkHighlightingWithCrLfEnsured()
    }

  @Test
  fun testTsconfigPriority() =
    checkHighlighting(ANGULAR_CORE_17_3_0, extension = "html", dir = true, configureFileName = "src/component.html",
                      configurators = listOf())

  @Test
  fun testDirectiveInputInNgTemplate() =
    checkHighlighting(extension = "ts")

  @Test
  fun testTuiLet() =
    checkHighlighting(ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testTypeofNg18() =
    checkHighlighting(ANGULAR_CORE_18_2_1, dir = true, configureFileName = "typeof.html")

  @Test
  fun testTypeofNg19() =
    checkHighlighting(ANGULAR_CORE_19_2_0, dir = true, configureFileName = "typeof.html")

  @Test
  fun testConfigWithMapping() =
    checkHighlighting(ANGULAR_CORE_18_2_1, dir = true, configureFileName = "projects/frontend/src/app/app.component.html",
                      configurators = listOf(Angular2TsExpectedConfigFiles("projects/frontend/tsconfig.app.json")))

  @Test
  fun testComponentWithParenthesis() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  @Test
  fun testHostBindings() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  @Test
  fun testHostBindingsSyntax() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts", checkSymbolNames = true)

  @Test
  fun testDirectiveSelectorsSyntax() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts", checkSymbolNames = true)

  @Test
  fun testBindingsSyntax() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts", checkSymbolNames = true)

  @Test
  fun testHostDirectivesSyntax() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts", checkSymbolNames = true)

  @Test
  fun testEs6ObjectInitializer() =
    checkHighlighting(ANGULAR_CORE_18_2_1, extension = "ts")

  @Test
  fun testUnusedTemplateVariable() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  // TODO - JSUnusedGlobalSymbolsPass doesn't support injections
  fun _testViewChildrenDecorator() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  @Test
  fun testViewChildrenDecoratorHtml() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, dir = true)

  @Test
  fun testViewChildrenDecoratorSyntax() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts", checkSymbolNames = true)

  @Test
  fun testViewChildrenSignal() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, dir = true)

  @Test
  fun testViewChildSwitchBlock() =
    checkHighlighting(ANGULAR_CORE_18_2_1, extension = "ts")

  @Test
  fun testViewChildrenSignalSyntax() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts", checkSymbolNames = true)

  @Test
  fun testReferenceOnTagWithTemplateBindings() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  @Test
  fun testTemplateBindingsNgFor() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  @Test
  fun testNoDuplicatedUnresolvedPipeError() =
    checkHighlighting(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1, extension = "ts")

  @Test
  fun testObjectInitializerInTemplate() =
    checkHighlighting(ANGULAR_CORE_18_2_1, dir = true, configureFileName = "app.component.html")

  @Test
  fun testTemplateLiteral18() =
    checkHighlighting(ANGULAR_CORE_18_2_1, extension = "html")

  @Test
  fun testTemplateLiteral19_2() =
    checkHighlighting(ANGULAR_CORE_19_2_0, extension = "html")

  @Test
  fun testTemplateLiteralInline18() =
    checkHighlighting(ANGULAR_CORE_18_2_1, extension = "ts")

  @Test
  fun testTemplateLiteralInline19_2() =
    checkHighlighting(ANGULAR_CORE_19_2_0, extension = "ts")

  @Test
  fun testInKeywordNg19() =
    checkHighlighting(ANGULAR_CORE_19_2_0, extension = "ts")

  @Test
  fun testInKeywordNg20() =
    checkHighlighting(ANGULAR_CORE_20_0_0_NEXT_3, extension = "ts")

  @Test
  fun testHostAttributeToken() =
    checkHighlighting(ANGULAR_CORE_19_2_0, dir = true, configureFileName = "app.component.html")

  @Test
  fun testHostAttributeTokenInline() =
    checkHighlighting(ANGULAR_CORE_19_2_0, extension = "ts")

  @Test
  fun testCreateComponentBindings() =
    checkHighlighting(ANGULAR_CORE_20_0_0_NEXT_3, extension = "ts")

  @Test
  fun testHostDirectiveWithInheritance() =
    checkHighlighting(ANGULAR_CORE_19_2_0, extension = "ts")

  @Test
  fun testNgContentElementSelector() =
    checkHighlighting(ANGULAR_CORE_19_2_0, extension = "ts")

  @Test
  fun testNgDeepSemantic() =
    checkHighlighting(ANGULAR_CORE_19_2_0,
                      extension = "css",
                      checkSymbolNames = true,
                      checkInformation = false,
                      checkWarnings = false,
                      checkWeakWarnings = false)

  @Test
  fun testSignalStore() =
    checkHighlighting(ANGULAR_CORE_20_1_4, NGRX_SIGNALS_20_1_0, extension = "ts",
                      checkSymbolNames = true)

  @Test
  fun testCssCustomProperty() =
    checkHighlighting(ANGULAR_CORE_19_2_0, extension = "ts")

  @Test
  fun testCssCustomPropertyExternalTemplate() =
    checkHighlighting(ANGULAR_CORE_19_2_0, dir = true, configureFileName = "test.css")

  @Test
  fun testNoKeyupEventCodeModifierNg13() =
    checkHighlighting(ANGULAR_CORE_13_3_5, extension = "html")

  @Test
  fun testKeyupEventCodeModifierNg15() =
    checkHighlighting(ANGULAR_CORE_15_1_5, extension = "html")

  @Test
  fun testKeydownCaseNonsensitive() =
    checkHighlighting(ANGULAR_CORE_15_1_5, extension = "html")

  @Test
  fun testVoidOperatorNg20() =
    checkHighlighting(ANGULAR_CORE_20_0_0_NEXT_3, extension = "html")

  @Test
  fun testVoidOperatorNg19() =
    checkHighlighting(ANGULAR_CORE_19_2_0, extension = "html")

  @Test
  fun testExponentialOperatorNg20() =
    checkHighlighting(ANGULAR_CORE_20_0_0_NEXT_3, extension = "html")

  @Test
  fun testExponentialOperatorNg19() =
    checkHighlighting(ANGULAR_CORE_19_2_0, extension = "html")

  @Test
  fun testAssignmentOperatorsNg20() =
    checkHighlighting(ANGULAR_CORE_20_0_0_NEXT_3, extension = "html", dir = true)

  @Test
  fun testAssignmentOperatorsNg20_1() =
    checkHighlighting(ANGULAR_CORE_20_1_4, extension = "html", dir = true)

  @Test
  fun testNewAngularAnimationAttributes() =
    checkHighlighting(ANGULAR_CORE_20_2_2, extension = "ts", checkSymbolNames = true)

  @Test
  fun testNewAngularAnimationBindings() =
    checkHighlighting(ANGULAR_CORE_20_2_2, extension = "ts", checkSymbolNames = true)

  @Test
  fun testNewAngularAnimationEvents() =
    checkHighlighting(ANGULAR_CORE_20_2_2, extension = "ts")

  @Test
  fun testIonic8() =
    checkHighlighting(ANGULAR_CORE_19_2_0, IONIC_ANGULAR_8_4_3, IONIC_CORE_8_4_3,
                      configurators = listOf(Angular2TsConfigFile(strictTemplates = true)),
                      configureFileName = "app.component.html", dir = true)

  @Test
  fun testDirectiveWithExportDefault() =
    checkHighlighting(ANGULAR_CORE_20_1_4, extension = "html", dir = true)

  @Test
  fun testUnusedSymbolSuppressionInBlock() =
    checkHighlighting(ANGULAR_CORE_20_1_4, extension = "html", dir = true)

  @Test
  fun testUnusedSymbolSuppressionInTemplateBindings() =
    checkHighlighting(ANGULAR_CORE_20_1_4, extension = "ts")

  @Test
  fun testListenerInNestedIfBlocks() =
    checkHighlighting(ANGULAR_CORE_20_1_4, extension = "ts")

  @Test
  fun testLibraryWithSignals() =
    checkHighlighting(ANGULAR_CORE_20_1_4, configureFileName = "my-component.ts", dir = true)

  @Test
  fun testConstAsSelector() =
    checkHighlighting(ANGULAR_CORE_21_2_0, extension = "ts")

  @Test
  fun testSpreadSyntax_V20() =
    checkHighlighting(ANGULAR_CORE_20_2_2, extension = "ts")

  @Test
  fun testSpreadSyntax() =
    checkHighlighting(ANGULAR_CORE_21_1_3, extension = "ts")

  @Test
  fun testArrowFunctions_V21_1() =
    checkHighlighting(ANGULAR_CORE_21_1_3, extension = "ts")

  @Test
  fun testArrowFunctions() =
    checkHighlighting(ANGULAR_CORE_21_2_0, extension = "ts")

  @Test
  fun testRegexes_V20() =
    checkHighlighting(ANGULAR_CORE_20_2_2, extension = "ts")

  @Test
  fun testRegexes() =
    checkHighlighting(ANGULAR_CORE_21_0_9, extension = "ts")

  @Test
  fun testInstanceof_V21_1() =
    checkHighlighting(ANGULAR_CORE_21_1_3, extension = "ts")

  @Test
  fun testInstanceof() =
    checkHighlighting(ANGULAR_CORE_21_2_0, extension = "ts")

  @Test
  fun testDirectAriaBindings_V20_1() =
    checkHighlighting(ANGULAR_CORE_20_1_4, extension = "ts")

  @Test
  fun testDirectAriaBindings() =
    checkHighlighting(ANGULAR_CORE_20_2_2, extension = "ts")

  @Test
  fun testImplicitUsages() =
    checkHighlighting(ANGULAR_CORE_21_0_9, NGRX_EFFECTS_21_0_1, RXJS_7_8_1, extension = "ts")

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
    configureFileName: String = getDefaultConfigureFileName(extension),
    checkSymbolNames: Boolean = false,
    checkInformation: Boolean = checkSymbolNames,
    checkWarnings: Boolean = true,
    checkWeakWarnings: Boolean = true,
    configurators: List<PolySymbolsTestConfigurator> = listOf(Angular2TsConfigFile(strictTemplates = strictTemplates)),
  ) {
    doHighlightingTest(
      *modules,
      dir = dir,
      extension = extension,
      configureFileName = configureFileName,
      configurators = configurators,
      checkInjections = checkInjections,
      checkSymbolNames = checkSymbolNames,
      checkInformation = checkInformation,
      checkWarnings = checkWarnings,
      checkWeakWarnings = checkWeakWarnings,
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