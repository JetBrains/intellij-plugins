package org.angular2.library.forms

import com.intellij.polySymbols.testFramework.moveToOffsetBySignature
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.TestTsGoProxy
import org.angular2.TestTsNode
import org.angular2.lang.Angular2Bundle
import org.junit.Test

@TestTsNode
@TestTsGoProxy
class Angular2FormsQuickFixesTest : Angular2TestCase("library/forms/quickFixes") {

  @Test
  fun testNestedFormGroupFromControlNameAttribute() =
    doQuickFixTest(
      "formControlName=\"fi<caret>rst\"",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "control", "name")
    )

  @Test
  fun testNestedFormGroupFromControlNameAttributeExternal() =
    doQuickFixTest(
      "formControlName=\"fi<caret>rst\"",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "control", "name"),
      dir = true,
      extension = "html",
    )

  @Test
  fun testNestedFormGroupFromGroupNameAttribute() =
    doQuickFixTest(
      "formGroupName=\"fi<caret>rst\"",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "group", "name")
    )

  @Test
  fun testNestedFormGroupFromArrayNameAttribute() =
    doQuickFixTest(
      "formArrayName=\"fi<caret>rst\"",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "array", "name")
    )

  @Test
  fun testNestedFormGroupFromCallLiteralControl() =
    doQuickFixTest(
      "this.form.get('name.fi<caret>rst');",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "control", "name")
    )

  @Test
  fun testNestedFormGroupFromCallLiteralForm() =
    doQuickFixTest(
      "this.form.get('name.fi<caret>rst');",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "group", "name")
    )

  @Test
  fun testNestedFormGroupFromCallLiteralArray() =
    doQuickFixTest(
      "this.form.get('name.fi<caret>rst');",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "array", "name")
    )

  @Test
  fun testNestedFormGroupFromCallArrayLiteralControl() =
    doQuickFixTest(
      "this.form.get(['name', 'fo<caret>o', 'bar'])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "foo", "control", "name")
    )

  @Test
  fun testNestedFormGroupFromCallArrayLiteralForm() =
    doQuickFixTest(
      "this.form.get(['name', 'fo<caret>o', 'bar'])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "foo", "group", "name")
    )

  @Test
  fun testNestedFormGroupFromCallArrayLiteralArray() =
    doQuickFixTest(
      "this.form.get(['name', 'fo<caret>o', 'bar'])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "foo", "array", "name")
    )

  @Test
  fun testFormBuilderRegularFromCallArrayLiteralArray() =
    doQuickFixTest(
      "this.form.get([\"name\", \"chec<caret>k\"])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "check", "array", "name")
    )

  @Test
  fun testFormBuilderRegularFromCallArrayLiteralControl() =
    doQuickFixTest(
      "this.form.get([\"name\", \"chec<caret>k\"])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "check", "control", "name")
    )

  @Test
  fun testFormBuilderRegularFromCallArrayLiteralGroup() =
    doQuickFixTest(
      "this.form.get([\"name\", \"chec<caret>k\"])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "check", "group", "name")
    )

  @Test
  fun testFormBuilderNewCallFromControlNameAttribute() =
    doQuickFixTest(
      "formControlName=\"f<caret>oo\"",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "foo", "control", "more")
    )

  private fun doQuickFixTest(
    signature: String,
    quickFixName: String,
    dir: Boolean = false,
    extension: String = "ts",
  ) = doConfiguredTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0,
                       dir = dir, checkResult = true, extension = extension) {
    checkHighlightingEx(checkInjections = true)
    moveToOffsetBySignature(signature)
    launchAction(findSingleIntention(quickFixName))
  }
}