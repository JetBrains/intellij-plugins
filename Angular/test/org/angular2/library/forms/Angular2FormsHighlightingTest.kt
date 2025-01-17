package org.angular2.library.forms

import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2FormsHighlightingTest : Angular2TestCase("library/forms/highlighting") {

  fun testNestedFormGroup() =
    checkHighlighting(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupSemantic() =
    checkHighlighting(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, checkSymbolNames = true)

  fun testNestedFormGroupControlUsageFromLiteral() =
    checkUsageHighlighting(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupControlUsageFromArrayLiteral() =
    checkUsageHighlighting(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupControlUsageFromDeclaration() =
    checkUsageHighlighting(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupControlUsageFromAttribute() =
    checkUsageHighlighting(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupGroupUsageFromLiteral() =
    checkUsageHighlighting(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupGroupUsageFromArrayLiteral() =
    checkUsageHighlighting(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupGroupUsageFromDeclaration() =
    checkUsageHighlighting(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  fun testNestedFormGroupGroupUsageFromAttribute() =
    checkUsageHighlighting(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

}