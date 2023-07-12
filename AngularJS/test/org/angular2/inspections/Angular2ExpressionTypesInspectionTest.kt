// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.lang.javascript.TypeScriptTestUtil
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.codeInsight.Angular2AttributesTest
import org.angular2.modules.Angular2TestModule
import org.angular2.modules.Angular2TestModule.Companion.configureCopy
import org.angular2.modules.Angular2TestModule.Companion.configureLink
import org.angularjs.AngularTestUtil

class Angular2ExpressionTypesInspectionTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass) + "expressionType"
  }

  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(Angular2TemplateInspectionsProvider(true))
  }

  fun testSimpleTypes() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14,
                  Angular2TestModule.ANGULAR_FORMS_8_2_14)
    myFixture.configureByFiles("simple.html", "simpleComponent.ts", "componentWithTypes.ts")
    myFixture.checkHighlighting()
  }

  fun testExpressions() {
    TypeScriptTestUtil.forceConfig(project, null, testRootDisposable)
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14,
                  Angular2TestModule.ANGULAR_FORMS_8_2_14, Angular2TestModule.RXJS_6_4_0)
    myFixture.configureByFiles("expressions.html", "expressions.ts", "componentWithTypes.ts")
    myFixture.checkHighlighting()
  }

  fun testTemplateBindings() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14)
    myFixture.configureByFiles("template.html", "template.ts")
    myFixture.checkHighlighting()
  }

  fun testGenericsValidation() {
    configureLink(myFixture)
    myFixture.configureByFiles("generics.html", "generics.ts")
    myFixture.checkHighlighting()
  }

  fun testNgForOfAnyTypeNonStrictTemplates() {
    TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14)
    myFixture.configureByFiles("ngForOfAnyType.ts")
    myFixture.checkHighlighting()
  }

  fun testNgForOfAnyTypeStrict() {
    configureCommonFiles()
    myFixture.configureByFiles("ngForOfAnyTypeStrict.ts")
    myFixture.checkHighlighting()
  }

  fun testAnyType() {
    TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    configureLink(myFixture)
    myFixture.configureByFiles("any-type.ts")
    myFixture.checkHighlighting()
  }

  fun testSlicePipe() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14)
    myFixture.configureByFiles("slice_pipe_test.ts")
    myFixture.checkHighlighting()
  }

  fun testNgForOfQueryList() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14)
    myFixture.configureByFiles("ngForOfQueryList.ts")
    myFixture.checkHighlighting()
  }

  fun testInputValue() {
    configureLink(myFixture)
    myFixture.configureByFiles("inputValue.html", "inputValue.ts")
    myFixture.checkHighlighting()
  }

  /**
   * @see Angular2AttributesTest.testTypeMismatchErrorWithOptionalInputs
   */
  fun testNullChecks() {
    configureCommonFiles()
    myFixture.configureByFiles("NullChecks.html", "NullChecks.ts")
    myFixture.checkHighlighting()
  }

  /**
   * @see Angular2AttributesTest.testTypeMismatchErrorWithOptionalInputs
   */
  fun testNullChecksInline() {
    configureCommonFiles()
    myFixture.configureByFiles("NullChecksInline.ts")
    myFixture.checkHighlighting()
  }

  fun testNgIfAsContextGuardStrictNullChecks() {
    configureCommonFiles()
    myFixture.configureByFiles("NgIfAsContextGuardStrictNullChecks.ts")
    myFixture.checkHighlighting()
  }

  fun testNgIfAsContextGuardRemovesFalsy() {
    configureCommonFiles()
    myFixture.configureByFiles("NgIfAsContextGuardRemovesFalsy.ts")
    myFixture.checkHighlighting()
  }

  fun testNgForContextGuard() {
    configureCommonFiles()
    myFixture.configureByFiles("NgForContextGuard.ts")
    myFixture.checkHighlighting()
  }

  fun testNgrxLetContextGuard() {
    configureCommonFiles()
    myFixture.configureByFile("let.directive.ts")
    myFixture.configureByFiles("NgrxLetContextGuard.ts")
    myFixture.checkHighlighting()
  }

  fun testNgTemplateContextGuardNonGeneric() {
    configureCommonFiles()
    myFixture.configureByFiles("ngTemplateContextGuardNonGeneric.ts")
    myFixture.checkHighlighting()
  }

  fun testNgTemplateContextGuardDoubleGeneric() {
    configureCommonFiles()
    myFixture.configureByFiles("ngTemplateContextGuardDoubleGeneric.ts")
    myFixture.checkHighlighting()
  }

  fun testNgTemplateContextGuardPartialGeneric() {
    configureCommonFiles()
    myFixture.configureByFiles("ngTemplateContextGuardPartialGeneric.ts")
    myFixture.checkHighlighting()
  }

  fun testNgTemplateContextGuardOmitted() {
    configureCommonFiles()
    myFixture.configureByFiles("ngTemplateContextGuardOmitted.ts")
    myFixture.checkHighlighting()
  }

  fun testNgTemplateContextGuardOmittedGenericType() {
    configureCommonFiles()
    myFixture.configureByFiles("ngTemplateContextGuardOmittedGenericType.ts")
    myFixture.checkHighlighting()
  }

  fun testNgTemplateContextGuardMalformed() {
    configureCommonFiles()
    myFixture.configureByFiles("ngTemplateContextGuardMalformed.ts")
    myFixture.checkHighlighting()
  }

  fun testNgTemplateContextGuardEmptyInputs() {
    configureCommonFiles()
    myFixture.configureByFiles("ngTemplateContextGuardEmptyInputs.ts")
    myFixture.checkHighlighting()
  }

  fun testNgTemplateContextGuardTwoDirectivesOneGuard() {
    configureCommonFiles()
    myFixture.configureByFiles("ngTemplateContextGuardTwoDirectivesOneGuard.ts")
    myFixture.checkHighlighting()
  }

  fun testNgTemplateContextGuardTwoGuards() {
    // the test documents an edge case, in Angular language-tools results are different, we have false-negative
    configureCommonFiles()
    myFixture.configureByFiles("ngTemplateContextGuardTwoGuards.ts")
    myFixture.checkHighlighting()
  }

  fun testNgTemplateContextGuardInferenceFromTwoInputs() {
    // There are 2 things of interest:
    // * type of person
    // * type checking for assignment of expressions to directive inputs
    configureCommonFiles()
    myFixture.configureByFiles("ngTemplateContextGuardInferenceFromTwoInputs.ts")
    myFixture.checkHighlighting()
  }

  fun testExpectedTypeTwoDirectives() {
    configureCommonFiles()
    myFixture.configureByFiles("expectedTypeTwoDirectives.ts")
    myFixture.checkHighlighting()
  }

  fun testExpectedTypeTwoDirectivesWithCommonType() {
    configureCommonFiles()
    myFixture.configureByFiles("expectedTypeTwoDirectivesWithCommonType.ts")
    myFixture.checkHighlighting()
  }

  fun testExpectedTypeGenericInferenceFromTwoInputs() {
    configureCommonFiles()
    myFixture.configureByFiles("expectedTypeGenericInferenceFromTwoInputs.ts")
    myFixture.checkHighlighting()
  }

  fun testGenericDirectiveReferenceNonStrict() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_MATERIAL_16_0_0_NEXT_6, Angular2TestModule.ANGULAR_CORE_16_0_0_NEXT_4,
                  Angular2TestModule.ANGULAR_COMMON_16_0_0_NEXT_4, Angular2TestModule.ANGULAR_FORMS_16_0_0_NEXT_4)
    myFixture.configureByFile("genericDirectiveReference.ts")
    myFixture.checkHighlighting()
  }

  fun testGenericDirectiveReferenceStrictTemplates() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_MATERIAL_16_0_0_NEXT_6, Angular2TestModule.ANGULAR_CORE_16_0_0_NEXT_4,
                  Angular2TestModule.ANGULAR_COMMON_16_0_0_NEXT_4, Angular2TestModule.ANGULAR_FORMS_16_0_0_NEXT_4)
    myFixture.configureByFile("tsconfig.json")
    myFixture.configureByFile("tsconfig.app.json")
    myFixture.configureByFile("genericDirectiveReference.ts")
    myFixture.checkHighlighting()
  }

  fun testGenericDirectiveReferenceUnsubstitutedFallsBackToAny() {
    configureCommonFiles()
    myFixture.configureByFiles("genericDirectiveReferenceUnsubstitutedFallsBackToAny.ts")
    myFixture.checkHighlighting()
  }

  private fun configureCommonFiles() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_15_1_5, Angular2TestModule.ANGULAR_COMMON_15_1_5,
                  Angular2TestModule.RXJS_6_4_0)
    myFixture.configureByFile("tsconfig.json")
    myFixture.configureByFile("tsconfig.app.json")
  }
}
