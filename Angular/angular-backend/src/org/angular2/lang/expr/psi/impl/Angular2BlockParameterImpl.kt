// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSStatement
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.parentOfType
import com.intellij.util.IncorrectOperationException
import org.angular2.codeInsight.blocks.Angular2BlockParameterPrefixSymbol
import org.angular2.codeInsight.blocks.Angular2BlockParameterSymbol
import org.angular2.lang.expr.lexer.Angular2TokenTypes
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.impl.Angular2BindingImpl.Companion.getExpression
import org.angular2.lang.html.psi.Angular2HtmlBlock

class Angular2BlockParameterImpl(elementType: IElementType?) : Angular2EmbeddedExpressionImpl(elementType), Angular2BlockParameter {
  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is Angular2ElementVisitor) {
      visitor.visitAngular2BlockParameter(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override fun getName(): String? = nameElement?.text

  override val prefix: String?
    get() = prefixElement?.text

  override val block: Angular2HtmlBlock?
    get() = parentOfType<Angular2HtmlBlock>()

  override val prefixDefinition: Angular2BlockParameterPrefixSymbol?
    get() {
      val prefixName = prefix ?: return null
      return block?.definition?.parameterPrefixes?.find { it.name == prefixName }
    }

  override val definition: Angular2BlockParameterSymbol?
    get() {
      val name = name ?: return null
      return block?.definition?.parameters?.let { definitions ->
        if (isPrimaryExpression)
          definitions.find { it.isPrimaryExpression }
        else {
          prefixDefinition?.parameters?.find { it.name == name }
          ?: definitions.find { it.name == name }
        }
      }
    }

  override val index: Int
    get() = block?.parameters?.indexOf(this) ?: 0

  override val isPrimaryExpression: Boolean
    get() = firstChild is JSExpression || firstChild is JSStatement

  override val nameElement: PsiElement?
    get() = node.getChildren(null)
      .firstOrNull { it.elementType == Angular2TokenTypes.BLOCK_PARAMETER_NAME }
      ?.psi

  override val prefixElement: PsiElement?
    get() = node.getChildren(null)
      .firstOrNull { it.elementType == Angular2TokenTypes.BLOCK_PARAMETER_PREFIX }
      ?.psi

  override val expression: JSExpression?
    get() = getExpression(this)

  override val variables: List<JSVariable>
    get() = children.mapNotNull { it as? JSVarStatement }
      .flatMap { it.variables.asSequence() }

  override fun setName(name: String): PsiElement {
    throw IncorrectOperationException()
  }
}