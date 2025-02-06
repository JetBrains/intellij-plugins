package org.angular2.library.forms.impl

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptNewExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSNamedType
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import org.angular2.library.forms.*

class Angular2FormSymbolsBuilder(private val consumer: (Angular2FormGroup) -> Unit) : JSRecursiveWalkingElementVisitor() {

  override fun visitJSVariable(node: JSVariable) {
    if (node is TypeScriptField) {
      buildFormSymbolFromInitializer(node, node.initializer)
        ?.asSafely<Angular2FormGroup>()
        ?.let(consumer)
    }
  }

  override fun visitJSFunctionDeclaration(node: JSFunction) {
    if (node.isConstructor) {
      super.visitJSFunctionDeclaration(node)
    }
  }

  override fun visitJSCallExpression(node: JSCallExpression) {
    if (!node.isNewExpression && getFormBuilderMethodName(node) == FORM_BUILDER_GROUP_METHOD) {
      val source =
        node.parent.asSafely<JSAssignmentExpression>()
          ?.definitionExpression
          ?.expression
          ?.asSafely<JSReferenceExpression>()
          ?.takeIf { it.qualifier is JSThisExpression }
          ?.resolve()
          ?.asSafely<TypeScriptField>()
        ?: return
      buildFormSymbolFromCallExpression(source, node)
        ?.asSafely<Angular2FormGroup>()
        ?.let(consumer)
    }
  }

  private fun buildFormSymbolFromInitializer(source: PsiElement, expression: JSExpression?): Angular2FormAbstractControl? =
    if (expression is JSCallExpression)
      if (expression is TypeScriptNewExpression)
        buildFormSymbolFromNewExpression(source, expression)
      else
        buildFormSymbolFromCallExpression(source, expression)
    else
      null

  private fun buildFormSymbolFromNewExpression(source: PsiElement, expression: TypeScriptNewExpression): Angular2FormAbstractControl? {
    val controlKind = expression
                        .methodExpression.asSafely<JSReferenceExpression>()
                        ?.takeIf { it.qualifier == null }
                        ?.referenceName
                      ?: return null
    return when (controlKind) {
      FORM_CONTROL_CONSTRUCTOR -> if (source is JSProperty) Angular2FormControlImpl(source) else null
      FORM_ARRAY_CONSTRUCTOR -> if (source is JSProperty) Angular2FormArrayImpl(source) else null
      FORM_GROUP_CONSTRUCTOR -> {
        val initializer = expression.arguments.getOrNull(0)?.asSafely<JSObjectLiteralExpression>()
        Angular2FormGroupImpl(source, initializer, buildFormSymbolsFromObjectLiteral(initializer, false))
      }
      else -> null
    }
  }

  private fun buildFormSymbolFromCallExpression(source: PsiElement, node: JSCallExpression): Angular2FormAbstractControl? {
    val methodName = getFormBuilderMethodName(node)
                       ?.takeIf { isFormBuilderMethodCall(node) }
                     ?: return null
    return when (methodName) {
      FORM_BUILDER_GROUP_METHOD -> {
        val initializer = node.arguments.getOrNull(0)?.asSafely<JSObjectLiteralExpression>()
        Angular2FormGroupImpl(source, initializer, buildFormSymbolsFromObjectLiteral(initializer, true))
      }
      FORM_BUILDER_CONTROL_METHOD -> if (source is JSProperty) Angular2FormControlImpl(source) else null
      FORM_BUILDER_ARRAY_METHOD -> if (source is JSProperty) Angular2FormArrayImpl(source) else null
      else -> null
    }
  }

  private fun getFormBuilderMethodName(node: JSCallExpression): String? =
    (node.methodExpression as? JSReferenceExpression)
      ?.takeIf { it.qualifier != null && it.qualifier !is JSThisExpression }
      ?.referenceName
      ?.takeIf { it == FORM_BUILDER_GROUP_METHOD || it == FORM_BUILDER_ARRAY_METHOD || it == FORM_BUILDER_CONTROL_METHOD }

  private fun isFormBuilderMethodCall(node: JSCallExpression): Boolean =
    (node.methodExpression as? JSReferenceExpression)
      ?.takeIf { it.qualifier != null && it.qualifier !is JSThisExpression }
      ?.let { JSResolveUtil.getExpressionJSType(it.qualifier) }
      ?.substitute()
      ?.asSafely<JSNamedType>()
      ?.qualifiedName
      ?.qualifiedName == FORM_BUILDER_TYPE

  private fun buildFormSymbolsFromObjectLiteral(objectLiteral: JSObjectLiteralExpression?, isFormBuilderCall: Boolean): List<Angular2FormAbstractControl> =
    objectLiteral?.properties?.mapNotNull { property ->
      property.initializer?.let {
        if (isFormBuilderCall && it is JSArrayLiteralExpression || it is JSLiteralExpression)
          Angular2FormControlImpl(property)
        else
          buildFormSymbolFromInitializer(property, it)
      }
    } ?: emptyList()

}