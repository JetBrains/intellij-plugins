package org.angular2.library.forms

import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.lang.Angular2Bundle

class Angular2FormsQuickFixesTest : Angular2TestCase("library/forms/quickFixes") {

  fun testNestedFormGroupFromControlNameAttribute() =
    doQuickFixTest(
      "formControlName=\"fi<caret>rst\"",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "control", "name")
    )

  fun testNestedFormGroupFromControlNameAttributeExternal() =
    doQuickFixTest(
      "formControlName=\"fi<caret>rst\"",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "control", "name"),
      dir = true,
      extension = "html",
    )

  fun testNestedFormGroupFromGroupNameAttribute() =
    doQuickFixTest(
      "formGroupName=\"fi<caret>rst\"",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "group", "name")
    )

  fun testNestedFormGroupFromArrayNameAttribute() =
    doQuickFixTest(
      "formArrayName=\"fi<caret>rst\"",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "array", "name")
    )

  fun testNestedFormGroupFromCallLiteralControl() =
    doQuickFixTest(
      "this.form.get('name.fi<caret>rst');",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "control", "name")
    )

  fun testNestedFormGroupFromCallLiteralForm() =
    doQuickFixTest(
      "this.form.get('name.fi<caret>rst');",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "group", "name")
    )

  fun testNestedFormGroupFromCallLiteralArray() =
    doQuickFixTest(
      "this.form.get('name.fi<caret>rst');",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "array", "name")
    )

  fun testNestedFormGroupFromCallArrayLiteralControl() =
    doQuickFixTest(
      "this.form.get(['name', 'fo<caret>o', 'bar'])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "foo", "control", "name")
    )

  fun testNestedFormGroupFromCallArrayLiteralForm() =
    doQuickFixTest(
      "this.form.get(['name', 'fo<caret>o', 'bar'])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "foo", "group", "name")
    )

  fun testNestedFormGroupFromCallArrayLiteralArray() =
    doQuickFixTest(
      "this.form.get(['name', 'fo<caret>o', 'bar'])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "foo", "array", "name")
    )

  fun testFormBuilderRegularFromCallArrayLiteralArray() =
    doQuickFixTest(
      "this.form.get([\"name\", \"chec<caret>k\"])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "check", "array", "name")
    )

  fun testFormBuilderRegularFromCallArrayLiteralControl() =
    doQuickFixTest(
      "this.form.get([\"name\", \"chec<caret>k\"])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "check", "control", "name")
    )

  fun testFormBuilderRegularFromCallArrayLiteralGroup() =
    doQuickFixTest(
      "this.form.get([\"name\", \"chec<caret>k\"])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "check", "group", "name")
    )

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