package org.angular2.library.forms

import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.SkipTsGoFork
import org.angular2.TestTsGoFork
import org.angular2.TestTsNode
import org.junit.Test

@TestTsNode
@TestTsGoFork
class Angular2FormsHighlightingTest : Angular2TestCase("library/forms/highlighting") {

  @Test
  fun testNestedFormGroup() =
    doHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0,
                       checkInjections = true)

  @Test
  fun testNestedFormArray() =
    doHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0,
                       checkInjections = true)

  @Test
  fun testNestedFormGroupSemantic() =
    doHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0,
                       checkSymbolNames = true, checkWarnings = false, checkWeakWarnings = false,
                       checkInjections = true)

  @Test
  fun testNestedFormArraySemantic() =
    doHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0,
                       checkSymbolNames = true, checkWarnings = false, checkWeakWarnings = false,
                       checkInjections = true)

  @Test
  fun testNestedFormGroupControlUsageFromLiteral() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  fun testNestedFormGroupControlUsageFromArrayLiteral() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  @SkipTsGoFork
  fun testNestedFormGroupControlUsageFromDeclaration() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  fun testNestedFormGroupControlUsageFromAttribute() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  @SkipTsGoFork
  fun testNestedFormGroupGroupUsageFromLiteral() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  fun testNestedFormGroupGroupUsageFromArrayLiteral() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  fun testNestedFormGroupGroupUsageFromDeclaration() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  @SkipTsGoFork
  fun testNestedFormGroupGroupUsageFromAttribute() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  fun testNestedFormArrayUsageFromLiteral() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  fun testNestedFormArrayUsageFromArrayLiteral() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  fun testNestedFormArrayUsageFromDeclaration() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

  @Test
  fun testNestedFormArrayUsageFromAttribute() =
    doUsageHighlightingTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0)

}