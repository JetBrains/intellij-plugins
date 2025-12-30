// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile
import org.angular2.codeInsight.deprecated.Angular2AttributesTest

class Angular2ExpressionTypesInspectionTest : Angular2TestCase("inspections/expressionType", true) {

  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(Angular2TemplateInspectionsProvider(true))
  }

  // TODO fails on server
  fun _testSimpleTypes() =
    withTypeScriptServerService(TypeScriptServerServiceImpl::class) {
      checkHighlighting(
        Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14, Angular2TestModule.ANGULAR_FORMS_8_2_14,
        dir = true,
        configureFileName = "simple.html"
      )
    }

  fun testExpressions() =
    checkHighlighting(Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8,
                      Angular2TestModule.ANGULAR_FORMS_16_2_8, Angular2TestModule.RXJS_7_8_1,
                      configurators = listOf(Angular2TsConfigFile()),
                      dir = true,
                      configureFileName = "expressions.html")

  fun testTemplateBindings() =
    withTypeScriptServerService(TypeScriptServerServiceImpl::class) {
      checkHighlighting(Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14,
                        dir = true,
                        configureFileName = "template.html")
    }

  fun testGenericsValidation() =
    withTypeScriptServerService(TypeScriptServerServiceImpl::class) {
      checkHighlighting(Angular2TestModule.ANGULAR_CORE_8_2_14,
                        dir = true,
                        configureFileName = "generics.html")
    }

  fun testGenericsValidationStrict() =
    checkHighlighting(Angular2TestModule.TS_LIB,
                      Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8,
                      configurators = listOf(Angular2TsConfigFile()))

  fun testNgForOfAnyTypeNonStrictTemplates() =
    withTypeScriptServerService(TypeScriptServerServiceImpl::class) {
      checkHighlighting(Angular2TestModule.TS_LIB,
                        Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14,
                        configurators = listOf(Angular2TsConfigFile(strict = false)))
    }


  fun testNgForOfAnyTypeStrict() =
    checkHighlightingNg15()

  fun testInputValue() =
    withTypeScriptServerService(TypeScriptServerServiceImpl::class) {
      checkHighlighting(Angular2TestModule.ANGULAR_CORE_8_2_14,
                        dir = true,
                        configureFileName = "inputValue.html")
    }

  /**
   * @see Angular2AttributesTest.testTypeMismatchErrorWithOptionalInputs
   */
  fun testNullChecks() =
    checkHighlightingNg15(dir = true)

  /**
   * @see Angular2AttributesTest.testTypeMismatchErrorWithOptionalInputs
   */
  fun testNullChecksInline() =
    checkHighlightingNg15()

  fun testNgIfAsContextGuardStrictNullChecks() =
    checkHighlightingNg15()

  fun testNgIfAsContextGuardRemovesFalsy() =
    checkHighlightingNg15()

  fun testNgForContextGuard() =
    checkHighlightingNg15()

  fun testNgrxLetContextGuard() =
    checkHighlightingNg15(dir = true, extension = "ts")

  fun testNgTemplateContextGuardNonGeneric() =
    checkHighlightingNg15()

  fun testNgTemplateContextGuardDoubleGeneric() =
    checkHighlightingNg15()

  fun testNgTemplateContextGuardPartialGeneric() =
    checkHighlightingNg15()

  fun testNgTemplateContextGuardOmitted() =
    checkHighlightingNg15()

  fun testNgTemplateContextGuardOmittedGenericType() =
    checkHighlightingNg15()

  fun testNgTemplateContextGuardMalformed() =
    checkHighlightingNg15()

  fun testNgTemplateContextGuardEmptyInputs() =
    checkHighlightingNg15()

  fun testNgTemplateContextGuardTwoDirectivesOneGuard() =
    checkHighlightingNg15()

  fun testNgTemplateContextGuardTwoGuards() =
    // the test documents an edge case, in Angular language-tools results are different, we have false-negative
    checkHighlightingNg15()

  fun testNgTemplateContextGuardInferenceFromTwoInputs() =
  // There are 2 things of interest:
  // * type of person
    // * type checking for assignment of expressions to directive inputs
    // TODO - consider replacing `null as any` in directive type constructor with `undefined` (TCB - tcbCallTypeCtor),
    //        which will not infer person type as `any` when other inputs are missing improving type checking
    checkHighlightingNg15()

  fun testExpectedTypeTwoDirectives() =
    checkHighlightingNg15()

  fun testExpectedTypeTwoDirectivesWithCommonType() =
    checkHighlightingNg15()

  fun testExpectedTypeGenericInferenceFromTwoInputs() =
    checkHighlightingNg15()

  fun testGenericDirectiveReferenceNonStrict() =
    checkHighlighting(Angular2TestModule.TS_LIB,
                      Angular2TestModule.ANGULAR_MATERIAL_16_2_8, Angular2TestModule.ANGULAR_CORE_16_2_8,
                      Angular2TestModule.ANGULAR_COMMON_16_2_8, Angular2TestModule.ANGULAR_FORMS_16_2_8,
                      configurators = listOf(Angular2TsConfigFile(strict = false, strictTemplates = false)),
                      configureFileName = "genericDirectiveReference.ts")

  fun testGenericDirectiveReferenceStrictTemplates() =
    checkHighlighting(Angular2TestModule.TS_LIB,
                      Angular2TestModule.ANGULAR_MATERIAL_16_2_8, Angular2TestModule.ANGULAR_CORE_16_2_8,
                      Angular2TestModule.ANGULAR_COMMON_16_2_8, Angular2TestModule.ANGULAR_FORMS_16_2_8,
                      configurators = listOf(Angular2TsConfigFile()),
                      configureFileName = "genericDirectiveReference.ts")

  fun testGenericDirectiveReferenceUnsubstitutedFallsBackToAny() =
    checkHighlightingNg15()

  private fun checkHighlightingNg15(dir: Boolean = false, extension: String = if (dir) "html" else "ts") =
    checkHighlighting(
      Angular2TestModule.TS_LIB, Angular2TestModule.ANGULAR_CORE_15_1_5,
      Angular2TestModule.ANGULAR_COMMON_15_1_5, Angular2TestModule.RXJS_6_4_0,
      configurators = listOf(Angular2TsConfigFile()),
      dir = dir, extension = extension
    )
}