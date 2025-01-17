package org.angular2.library.forms.impl

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptNewExpression
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol
import org.angular2.library.forms.Angular2FormAbstractControl
import org.angular2.library.forms.Angular2FormGroup
import org.angular2.library.forms.FORM_CONTROL_CONSTRUCTOR
import org.angular2.library.forms.FORM_GROUP_CONSTRUCTOR

class Angular2FormSymbolsBuilder(private val consumer: (Angular2FormGroup) -> Unit) : JSRecursiveWalkingElementVisitor() {

  override fun visitJSVariable(node: JSVariable) {
    if (node is TypeScriptField) {
      buildFormSymbolFromInitializer(node, node.initializer ?: return)
        ?.asSafely<Angular2FormGroup>()
        ?.let(consumer)
    }
  }

  private fun buildFormSymbolFromInitializer(source: PsiElement, expression: JSExpression): Angular2FormAbstractControl? {
    val controlKind = (expression as? TypeScriptNewExpression ?: return null)
                        .methodExpression.asSafely<JSReferenceExpression>()
                        ?.takeIf { it.qualifier == null }
                        ?.referenceName
                      ?: return null
    return when (controlKind) {
      FORM_CONTROL_CONSTRUCTOR -> if (source is JSProperty) Angular2FormControlImpl(source) else null
      FORM_GROUP_CONSTRUCTOR -> {
        val initializer = expression.arguments.getOrNull(0)?.asSafely<JSObjectLiteralExpression>()
        Angular2FormGroupImpl(source, initializer, buildFormSymbolsFromObjectLiteral(initializer))
      }
      else -> null
    }
  }

  private fun buildFormSymbolsFromObjectLiteral(objectLiteral: JSObjectLiteralExpression?): List<Angular2FormAbstractControl> =
    objectLiteral?.properties?.mapNotNull { property ->
      property.initializer?.let { buildFormSymbolFromInitializer(property, it) }
    } ?: emptyList()

}