// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile
import org.angular2.TestTsGoProxy
import org.angular2.TestTsNode
import org.angular2.codeInsight.deprecated.Angular2AttributesTest
import org.junit.Test

@TestTsNode
@TestTsGoProxy
class Angular2ExpressionTypesInspectionTest : Angular2TestCase("inspections/expressionType") {

  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(Angular2TemplateInspectionsProvider(true))
  }

  // TODO fails on server
  fun _testSimpleTypes() =
    doHighlightingTest(
      Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14, Angular2TestModule.ANGULAR_FORMS_8_2_14,
      dir = true,
      configureFileName = "simple.html"
    )

  @Test
  fun testExpressions() =
    doHighlightingTest(Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8,
                       Angular2TestModule.ANGULAR_FORMS_16_2_8, Angular2TestModule.RXJS_7_8_1,
                       configurators = listOf(Angular2TsConfigFile()),
                       dir = true,
                       configureFileName = "expressions.html")

  @Test
  fun testTemplateBindings() =
    doHighlightingTest(Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14,
                       dir = true,
                       configureFileName = "template.html")

  @Test
  fun testGenericsValidation() =
    doHighlightingTest(Angular2TestModule.ANGULAR_CORE_8_2_14,
                       dir = true,
                       configureFileName = "generics.html")

  @Test
  fun testGenericsValidationStrict() =
    doHighlightingTest(Angular2TestModule.TS_LIB,
                       Angular2TestModule.ANGULAR_CORE_16_2_8, Angular2TestModule.ANGULAR_COMMON_16_2_8,
                       configurators = listOf(Angular2TsConfigFile()))

  @Test
  fun testNgForOfAnyTypeNonStrictTemplates() =
    doHighlightingTest(Angular2TestModule.TS_LIB,
                       Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14,
                       configurators = listOf(Angular2TsConfigFile(strict = false)))


  @Test
  fun testNgForOfAnyTypeStrict() =
    checkHighlightingNg15()

  @Test
  fun testInputValue() =
    doHighlightingTest(Angular2TestModule.ANGULAR_CORE_8_2_14,
                       dir = true,
                       configureFileName = "inputValue.html")

  /**
   * @see Angular2AttributesTest.testTypeMismatchErrorWithOptionalInputs
   */
  @Test
  fun testNullChecks() =
    checkHighlightingNg15(dir = true)

  /**
   * @see Angular2AttributesTest.testTypeMismatchErrorWithOptionalInputs
   */
  @Test
  fun testNullChecksInline() =
    checkHighlightingNg15()

  @Test
  fun testNgIfAsContextGuardStrictNullChecks() =
    checkHighlightingNg15()

  @Test
  fun testNgIfAsContextGuardRemovesFalsy() =
    checkHighlightingNg15()

  @Test
  fun testNgForContextGuard() =
    checkHighlightingNg15()

  @Test
  fun testNgrxLetContextGuard() =
    checkHighlightingNg15(dir = true, extension = "ts")

  @Test
  fun testNgTemplateContextGuardNonGeneric() =
    checkHighlightingNg15()

  @Test
  fun testNgTemplateContextGuardDoubleGeneric() =
    checkHighlightingNg15()

  @Test
  fun testNgTemplateContextGuardPartialGeneric() =
    checkHighlightingNg15()

  @Test
  fun testNgTemplateContextGuardOmitted() =
    checkHighlightingNg15()

  @Test
  fun testNgTemplateContextGuardOmittedGenericType() =
    checkHighlightingNg15()

  @Test
  fun testNgTemplateContextGuardMalformed() =
    checkHighlightingNg15()

  @Test
  fun testNgTemplateContextGuardEmptyInputs() =
    checkHighlightingNg15()

  @Test
  fun testNgTemplateContextGuardTwoDirectivesOneGuard() =
    checkHighlightingNg15()

  @Test
  fun testNgTemplateContextGuardTwoGuards() =
    // the test documents an edge case, in Angular language-tools results are different, we have false-negative
    checkHighlightingNg15()

  @Test
  fun testNgTemplateContextGuardInferenceFromTwoInputs() =
  // There are 2 things of interest:
  // * type of person
  // * type checking for assignment of expressions to directive inputs
  // TODO - consider replacing `null as any` in directive type constructor with `undefined` (TCB - tcbCallTypeCtor),
    //        which will not infer person type as `any` when other inputs are missing improving type checking
    checkHighlightingNg15()

  @Test
  fun testExpectedTypeTwoDirectives() =
    checkHighlightingNg15()

  @Test
  fun testExpectedTypeTwoDirectivesWithCommonType() =
    checkHighlightingNg15()

  @Test
  fun testExpectedTypeGenericInferenceFromTwoInputs() =
    checkHighlightingNg15()

  @Test
  fun testGenericDirectiveReferenceNonStrict() =
    doHighlightingTest(Angular2TestModule.TS_LIB,
                       Angular2TestModule.ANGULAR_MATERIAL_16_2_8, Angular2TestModule.ANGULAR_CORE_16_2_8,
                       Angular2TestModule.ANGULAR_COMMON_16_2_8, Angular2TestModule.ANGULAR_FORMS_16_2_8,
                       configurators = listOf(Angular2TsConfigFile(strict = false, strictTemplates = false)),
                       configureFileName = "genericDirectiveReference.ts")

  @Test
  fun testGenericDirectiveReferenceStrictTemplates() =
    doHighlightingTest(Angular2TestModule.TS_LIB,
                       Angular2TestModule.ANGULAR_MATERIAL_16_2_8, Angular2TestModule.ANGULAR_CORE_16_2_8,
                       Angular2TestModule.ANGULAR_COMMON_16_2_8, Angular2TestModule.ANGULAR_FORMS_16_2_8,
                       configurators = listOf(Angular2TsConfigFile()),
                       configureFileName = "genericDirectiveReference.ts")

  @Test
  fun testGenericDirectiveReferenceUnsubstitutedFallsBackToAny() =
    checkHighlightingNg15()

  private fun checkHighlightingNg15(dir: Boolean = false, extension: String = if (dir) "html" else "ts") =
    doHighlightingTest(
      Angular2TestModule.TS_LIB, Angular2TestModule.ANGULAR_CORE_15_1_5,
      Angular2TestModule.ANGULAR_COMMON_15_1_5, Angular2TestModule.RXJS_6_4_0,
      configurators = listOf(Angular2TsConfigFile()),
      dir = dir, extension = extension
    )
}