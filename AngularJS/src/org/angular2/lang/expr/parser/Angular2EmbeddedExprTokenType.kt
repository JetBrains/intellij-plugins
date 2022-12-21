// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser

import com.intellij.html.embedding.HtmlCustomEmbeddedContentTokenType
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lexer.Lexer
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.lexer.Angular2Lexer
import org.angular2.lang.html.XmlASTWrapperPsiElement
import org.jetbrains.annotations.NonNls
import java.util.*

class Angular2EmbeddedExprTokenType : HtmlCustomEmbeddedContentTokenType {

  private val myExpressionType: ExpressionType
  private val myTemplateKey: String?

  private constructor(@NonNls debugName: String, expressionType: ExpressionType)
    : super(debugName, Angular2Language.INSTANCE) {
    myExpressionType = expressionType
    myTemplateKey = null
  }

  private constructor(@NonNls debugName: String, expressionType: ExpressionType, @NonNls templateKey: String?)
    : super(debugName, Angular2Language.INSTANCE, false) {
    myExpressionType = expressionType
    myTemplateKey = templateKey
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val type = other as Angular2EmbeddedExprTokenType?
    return myExpressionType == type!!.myExpressionType && myTemplateKey == type.myTemplateKey
  }

  override fun hashCode(): Int {
    return Objects.hash(super.hashCode(), myExpressionType, myTemplateKey)
  }

  override fun createLexer(): Lexer {
    return Angular2Lexer()
  }

  override fun parse(builder: PsiBuilder) {
    myExpressionType.parse(builder, this, myTemplateKey)
  }

  override fun createPsi(node: ASTNode): PsiElement {
    return XmlASTWrapperPsiElement(node)
  }

  enum class ExpressionType(private val myParseMethod: (PsiBuilder, IElementType) -> Unit) {
    ACTION({ builder, root -> Angular2Parser.parseAction(builder, root) }),
    BINDING({ builder, root -> Angular2Parser.parseBinding(builder, root) }),
    INTERPOLATION({ builder, root -> Angular2Parser.parseInterpolation(builder, root) }),
    SIMPLE_BINDING({ builder, root -> Angular2Parser.parseSimpleBinding(builder, root) }),
    TEMPLATE_BINDINGS({ _, _ -> });

    fun parse(builder: PsiBuilder, root: IElementType, templateKey: String?) {
      if (this == TEMPLATE_BINDINGS) {
        assert(templateKey != null)
        Angular2Parser.parseTemplateBindings(builder, root, templateKey!!)
      }
      else {
        myParseMethod(builder, root)
      }
    }
  }

  companion object {

    @JvmField
    val ACTION_EXPR = Angular2EmbeddedExprTokenType(
      "NG:ACTION_EXPR", ExpressionType.ACTION)

    @JvmField
    val BINDING_EXPR = Angular2EmbeddedExprTokenType(
      "NG:BINDING_EXPR", ExpressionType.BINDING)

    @JvmField
    val INTERPOLATION_EXPR = Angular2EmbeddedExprTokenType(
      "NG:INTERPOLATION_EXPR", ExpressionType.INTERPOLATION)

    @JvmField
    val SIMPLE_BINDING_EXPR = Angular2EmbeddedExprTokenType(
      "NG:SIMPLE_BINDING_EXPR", ExpressionType.SIMPLE_BINDING)

    @JvmStatic
    fun createTemplateBindings(templateKey: String): Angular2EmbeddedExprTokenType {
      return Angular2EmbeddedExprTokenType("NG:TEMPLATE_BINDINGS_EXPR", ExpressionType.TEMPLATE_BINDINGS, templateKey)
    }
  }
}