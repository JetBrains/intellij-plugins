package org.angular2.library.forms

import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2FormsRenameRefactoringTest : Angular2TestCase("library/forms/rename") {

  fun testNestedFormGroupControlAttribute() =
    checkSymbolRename("newName", Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, dir = false)

  fun testNestedFormGroupGetLiteral() =
    checkSymbolRename("newName", Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, dir = false)

  fun testNestedFormGroupGetArrayLiteral() =
    checkSymbolRename("newName", Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, dir = false)

  fun testNestedFormGroupProperty() =
    checkSymbolRename("newName", Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, dir = false)

  fun testFormBuilderInConstructor() =
    checkSymbolRename("newName", Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, dir = false)

}