package org.angular2.library.forms

import com.intellij.webSymbols.moveToOffsetBySignature
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.lang.Angular2Bundle

class Angular2FormsQuickFixesTest : Angular2TestCase("library/forms/quickFixes") {

  fun testNestedFormGroupFromControlNameAttribute() =
    doQuickFixTest(
      "formControlName=\"fi<caret>rst\"",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "Control", "name")
    )

  fun testNestedFormGroupFromControlNameAttributeExternal() =
    doQuickFixTest(
      "formControlName=\"fi<caret>rst\"",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "Control", "name"),
      dir = true,
      extension = "html",
    )

  fun testNestedFormGroupFromGroupNameAttribute() =
    doQuickFixTest(
      "formGroupName=\"fi<caret>rst\"",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "Group", "name")
    )

  fun testNestedFormGroupFromCallLiteralControl() =
    doQuickFixTest(
      "this.form.get('name.fi<caret>rst');",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "Control", "name")
    )

  fun testNestedFormGroupFromCallLiteralForm() =
    doQuickFixTest(
      "this.form.get('name.fi<caret>rst');",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "first", "Group", "name")
    )

  fun testNestedFormGroupFromCallArrayLiteralControl() =
    doQuickFixTest(
      "this.form.get(['name', 'fo<caret>o', 'bar'])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "foo", "Control", "name")
    )

  fun testNestedFormGroupFromCallArrayLiteralForm() =
    doQuickFixTest(
      "this.form.get(['name', 'fo<caret>o', 'bar'])",
      Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name", "foo", "Group", "name")
    )

  private fun doQuickFixTest(
    signature: String,
    quickFixName: String,
    dir: Boolean = false,
    extension: String = "ts"
  ) = doConfiguredTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0,
                       dir = dir, checkResult = true, extension = extension) {
    loadInjectionsAndCheckHighlighting(false)
    moveToOffsetBySignature(signature)
    launchAction(findSingleIntention(quickFixName))
  }
}