// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.SyntaxTraverser
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.runInEdtAndWait
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.*
import org.angular2.Angular2TsConfigFile
import org.angular2.codeInsight.inspections.Angular2ExpressionTypesInspectionTest
import java.io.File

class Angular2HighlightingTest : Angular2TestCase("highlighting") {

  fun testSvgTags() = checkHighlighting(ANGULAR_COMMON_16_2_8, extension = "ts")

  fun testDeprecated() = checkHighlighting(dir = true)

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
  fun testTypeMismatchErrorWithOptionalInputs() = checkHighlighting(dir = true, extension = "ts", strictTemplates = true)

  fun testHostDirectives() = checkHighlighting(dir = true)

  fun testAnimationCallbacks() = checkHighlighting(dir = true)

  fun testElementShims() = checkHighlighting(dir = true)

  fun testCustomUserEvents() = checkHighlighting(dir = true)

  fun testFxLayout() = checkHighlighting(ANGULAR_CORE_9_1_1_MIXED, ANGULAR_FLEX_LAYOUT_13_0_0)

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
  fun testNgrxLetAsContextGuard() = checkHighlighting(ANGULAR_COMMON_13_3_5, dir = true, extension = "ts", strictTemplates = true)

  fun testRequiredInputs() = checkHighlighting(extension = "ts")

  fun testStaticAttributes() = checkHighlighting(dir = true)

  fun testSelfClosedTags() = checkHighlighting(dir = true)

  fun testTrUnderTemplate() = checkHighlighting(ANGULAR_CDK_14_2_0, dir = true)

  fun testDivUnderButton() = checkHighlighting(ANGULAR_MATERIAL_16_2_8, dir = true)

  fun testReadOnlyTemplateVariables() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, extension = "ts")

  fun testDirectiveWithStandardAttrSelector() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8,
                                                                  strictTemplates = true, extension = "ts")

  fun testInputsWithTransform() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, strictTemplates = true, dir = true)

  fun testOneTimeBindingOfPrimitives() = checkHighlighting(dir = true, configureFileName = "one_time_binding.html")

  fun testOneTimeBindingOfPrimitivesStrict() = checkHighlighting(dir = true, configureFileName = "one_time_binding.html",
                                                                 strictTemplates = true)

  fun testNgCspNonceNg15() = checkHighlighting(ANGULAR_CORE_15_1_5)

  fun testNgCspNonceNg16() = checkHighlighting(ANGULAR_CORE_16_2_8)

  fun testUndefinedInterpolationBinding() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_ROUTER_16_2_8, dir = true,
                                                              configureFileName = "hero-search.component.html")

  fun testSubjectGenericInference() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, RXJS_7_8_1,
                                                        extension = "ts", strictTemplates = true)

  fun testComplexFormControls() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8,
                                                    extension = "ts", strictTemplates = true)

  fun testSignalsColors() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8,
                                              extension = "ts", checkInformation = true)

  fun testSignalsColorsHtml() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, dir = true,
                                                  configureFileName = "signalsColors.html", checkInformation = true)

  fun testTemplateColorsHtml() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8, dir = true,
                                                   configureFileName = "colors.html", checkInformation = true)

  fun testRainbowColorsHtml() = doConfiguredTest(ANGULAR_CORE_16_2_8, ANGULAR_COMMON_16_2_8, ANGULAR_FORMS_16_2_8, dir = true,
                                                 configureFile = false) {
    myFixture.testRainbow("colors.html", FileUtil.loadFile(File("$testDataPath/$testName/colors.html")), true, true)
  }

  fun testBlockDefer() = checkHighlighting(ANGULAR_CORE_17_0_0_RC_0, extension = "ts")

  fun testBlockFor() = checkHighlighting(ANGULAR_CORE_17_0_0_RC_0, extension = "ts")

  fun testBlockForMultiVars() = checkHighlighting(ANGULAR_CORE_17_0_0_RC_0, extension = "ts")

  fun testBlockIf() = checkHighlighting(ANGULAR_CORE_17_0_0_RC_0, extension = "ts")

  fun testBlockSwitch() = checkHighlighting(ANGULAR_CORE_17_0_0_RC_0, extension = "ts")

  fun testInterpolationStrictMode() = checkHighlighting(ANGULAR_CORE_16_2_8, strictTemplates = true, extension = "ts")

  fun testOneTimeAttributesWithoutValueInStrictMode() = checkHighlighting(ANGULAR_CORE_16_2_8, ANGULAR_MATERIAL_16_2_8,
                                                                          strictTemplates = true, extension = "ts")

  fun testNgSrcAttribute() = checkHighlighting(ANGULAR_CORE_17_0_0_RC_0, ANGULAR_COMMON_17_0_0_RC_0,
                                               strictTemplates = true, extension = "ts", dir = true)

  fun testIfBlockAsVarType() = checkHighlighting(ANGULAR_CORE_17_0_0_RC_0, ANGULAR_COMMON_17_0_0_RC_0,
                                                 strictTemplates = true, extension = "ts")

  fun testForBlockSemanticOfHighlighting() = checkHighlighting(ANGULAR_CORE_17_0_0_RC_0,
                                                               strictTemplates = true, extension = "ts", checkInformation = true)

  fun testForBlockVarType() = checkHighlighting(ANGULAR_CORE_17_0_0_RC_0,
                                                strictTemplates = true, extension = "ts")

  fun testForBlockIterableType() = checkHighlighting(ANGULAR_CORE_17_0_0_RC_0,
                                                     strictTemplates = true, extension = "ts")

  fun testDeferBlockSemanticHighlighting() = checkHighlighting(ANGULAR_CORE_17_0_0_RC_0,
                                                               strictTemplates = true, extension = "html", checkInformation = true)

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
    checkInformation: Boolean = false,
  ) {
    doConfiguredTest(*modules, dir = dir, extension = extension, configureFileName = configureFileName,
                     configurators = listOf(Angular2TsConfigFile(strictTemplates = strictTemplates))
    ) {
      if (checkInjections)
        loadInjectionsAndCheckHighlighting(checkInformation)
      else
        checkHighlighting(true, checkInformation, true)
    }
  }

  private fun loadInjectionsAndCheckHighlighting(checkInformation: Boolean) {
    val data = ExpectedHighlightingData(
      myFixture.getEditor().getDocument(), true, true, checkInformation, true)
    data.init()
    runInEdtAndWait { PsiDocumentManager.getInstance(myFixture.getProject()).commitAllDocuments() }
    val injectedLanguageManager = InjectedLanguageManager.getInstance(myFixture.getProject())
    // We need to ensure that injections are cached before we check deprecated highlighting
    SyntaxTraverser.psiTraverser(myFixture.getFile())
      .forEach { if (it is PsiLanguageInjectionHost) injectedLanguageManager.getInjectedPsiFiles(it) }
    (myFixture as CodeInsightTestFixtureImpl).collectAndCheckHighlighting(data)
  }

}