package org.angular2.library.forms

import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2FormsHighlightingTest : Angular2TestCase("library/forms/highlighting") {

  fun testNestedFormGroup() =
    doHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0,
                       checkInjections = true)

  fun testNestedFormArray() =
    doHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0,
                       checkInjections = true)

  fun testNestedFormGroupSemantic() =
    doHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0,
                       checkSymbolNames = true, checkWarnings = false, checkWeakWarnings = false,
                       checkInjections = true)

  fun testNestedFormArraySemantic() =
    doHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0,
                       checkSymbolNames = true, checkWarnings = false, checkWeakWarnings = false,
                       checkInjections = true)

  fun testNestedFormGroupControlUsageFromLiteral() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupControlUsageFromArrayLiteral() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupControlUsageFromDeclaration() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupControlUsageFromAttribute() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupGroupUsageFromLiteral() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupGroupUsageFromArrayLiteral() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupGroupUsageFromDeclaration() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupGroupUsageFromAttribute() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormArrayUsageFromLiteral() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormArrayUsageFromArrayLiteral() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormArrayUsageFromDeclaration() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormArrayUsageFromAttribute() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

}