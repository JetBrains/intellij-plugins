package org.angular2.library.forms

import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.SkipTsGoProxy
import org.angular2.TestTsGoProxy
import org.angular2.TestTsNode
import org.junit.Test

@TestTsNode
@TestTsGoProxy
class Angular2FormsRenameRefactoringTest : Angular2TestCase("library/forms/rename") {

  @Test
  fun testNestedFormGroupControlAttribute() =
    doSymbolRename("newName", Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, dir = false)

  @Test
  fun testNestedFormGroupGetLiteral() =
    doSymbolRename("newName", Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, dir = false)

  @Test
  fun testNestedFormGroupGetArrayLiteral() =
    doSymbolRename("newName", Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, dir = false)

  @Test
  fun testNestedFormGroupProperty() =
    doSymbolRename("newName", Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, dir = false)

  @Test
  @SkipTsGoProxy // Invalid line
  fun testFormBuilderInConstructor() =
    doSymbolRename("newName", Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, dir = false)

}