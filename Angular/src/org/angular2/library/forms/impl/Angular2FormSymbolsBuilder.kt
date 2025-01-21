package org.angular2.library.forms.impl

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptNewExpression
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import org.angular2.library.forms.*

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
      FORM_ARRAY_CONSTRUCTOR -> if (source is JSProperty) Angular2FormArrayImpl(source) else null
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