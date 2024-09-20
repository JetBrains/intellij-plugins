package org.angular2.refactoring.inline

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.refactoring.inline.JSInlineHandler
import com.intellij.lang.javascript.refactoring.inline.JSVarOrFieldInliner
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.asSafely
import org.angular2.lang.html.psi.Angular2HtmlBlock

class Angular2LetVariableInliner(element: JSVariable, settings: MySettings) : JSVarOrFieldInliner(element, settings) {

  override fun getRefactoringId(): String {
    return "refactoring.angular.inline"
  }

  override fun removeDefinition(element: PsiElement) {
    JSInlineHandler.deleteElementWithReformat(mySettings.target.parentOfType<Angular2HtmlBlock>()!!)
  }

  override fun doInlineUsage(usageElement: JSReferenceExpression): JSInlineHandlerResult {
    val result = super.doInlineUsage(usageElement)
    val attribute = result.element?.parentOfType<XmlAttribute>()
                    ?: return result
    val quote = attribute.valueElement?.text?.getOrNull(0)?.takeIf { it == '\'' || it == '"' }
                ?: return result
    return JSInlineHandlerResult(prepareStringsForAttributes(result.element ?: return result, quote), result.additionalElementsToImport)
  }

  private fun prepareStringsForAttributes(element: PsiElement, attrQuote: Char): PsiElement {
    val strQuote = if (attrQuote == '\'') '"' else '\''
    if (element is JSLiteralExpression && element.isStringLiteral) {
      return element.replace(buildStringLiteralExpression(attrQuote, strQuote, element.value.asSafely<String>() ?: return element))
    }
    val strings = mutableListOf<JSLiteralExpression>()
    element.accept(object : JSRecursiveWalkingElementVisitor() {
      override fun visitJSLiteralExpression(node: JSLiteralExpression) {
        if (node.isStringLiteral) {
          strings.add(node)
        }
      }
    })
    strings.forEach { literal ->
      literal.value?.asSafely<String>()?.let { literal.replace(buildStringLiteralExpression(attrQuote, strQuote, it)) }
    }
    return element
  }

  private fun buildStringLiteralExpression(attrQuote: Char, quote: Char, text: String): JSLiteralExpression =
    (JSChangeUtil.createExpressionWithContext(
      quote + text
        .replace("$attrQuote", if (attrQuote == '\'') "&apos;" else "&quot;")
        .replace("$quote", "\\" + quote) + quote, myElement) as CompositeElement).psi as JSLiteralExpression

}