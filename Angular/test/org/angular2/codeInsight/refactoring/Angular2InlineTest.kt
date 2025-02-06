package org.angular2.codeInsight.refactoring

import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.refactoring.inline.JSInlineHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.refactoring.inline.Angular2InlineHandler

class Angular2InlineTest : Angular2TestCase("refactoring/inline", false) {

  fun testLetVariableMulti() =
    doTest(false, Angular2TestModule.ANGULAR_CORE_18_2_1)

  fun testLetVariableSingle() =
    doTest(true, Angular2TestModule.ANGULAR_CORE_18_2_1)

  fun testLetVariableComplexExpr() =
    doTest(false, Angular2TestModule.ANGULAR_CORE_18_2_1)

  private fun doTest(onlyOneRef: Boolean, vararg modules: WebFrameworkTestModule, ) =
    doConfiguredTest(*modules, extension = "html", checkResult = true) {
      invokeHandler(onlyOneRef)
    }

  private fun invokeHandler(onlyOneRef: Boolean) {
    val targetElement = myFixture.elementAtCaret
    val handler = getInlineHandler(onlyOneRef)
    handler.inlineElement(project, myFixture.editor, targetElement)
  }

  private fun getInlineHandler(onlyOneRef: Boolean): JSInlineHandler =
    object : Angular2InlineHandler() {
      override fun getSettingsForElement(
        element: PsiElement,
        editor: Editor,
        invocationReference: PsiReference?,
        elementUsages: NotNullLazyValue<Collection<PsiReference>>,
      ): Settings? {
        val settings = super.getSettingsForElement(element, editor, invocationReference, elementUsages)
        settings?.setOneRefToInline(onlyOneRef)
        return settings
      }
    }
}