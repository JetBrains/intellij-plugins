// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser

import com.intellij.html.embedding.HtmlCustomEmbeddedContentTokenType
import com.intellij.javascript.web.html.XmlASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lexer.Lexer
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.util.ThreeState
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.lexer.Angular2Lexer
import org.angular2.lang.html.Angular2TemplateSyntax
import org.jetbrains.annotations.NonNls

open class Angular2EmbeddedExprTokenType : HtmlCustomEmbeddedContentTokenType {

  protected val templateSyntax: Angular2TemplateSyntax
  protected val expressionType: ExpressionType
  protected val name: String?
  protected val index: Int

  private constructor(templateSyntax: Angular2TemplateSyntax, @NonNls debugName: String, expressionType: ExpressionType)
    : super(debugName, Angular2Language) {
    this.templateSyntax = templateSyntax
    this.expressionType = expressionType
    name = null
    index = -1
  }

  private constructor(templateSyntax: Angular2TemplateSyntax, @NonNls debugName: String, expressionType: ExpressionType, @NonNls templateKey: String?)
    : super("$debugName ($templateKey)", Angular2Language, false) {
    this.templateSyntax = templateSyntax
    this.expressionType = expressionType
    name = templateKey
    index = -1
  }

  private constructor(templateSyntax: Angular2TemplateSyntax, @NonNls debugName: String, expressionType: ExpressionType, @NonNls blockName: String?, parameterIndex: Int)
    : super("$debugName ($blockName:$parameterIndex)", Angular2Language, false) {
    this.templateSyntax = templateSyntax
    this.expressionType = expressionType
    name = blockName
    index = parameterIndex
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Angular2EmbeddedExprTokenType || javaClass != other.javaClass) return false
    return templateSyntax == other.templateSyntax
           && expressionType == other.expressionType
           && name == other.name
           && index == other.index
  }


  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + templateSyntax.hashCode()
    result = 31 * result + expressionType.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + index
    return result
  }

  override fun createLexer(): Lexer =
    Angular2Lexer(Angular2Lexer.RegularBinding(templateSyntax))

  override fun parse(builder: PsiBuilder) {
    expressionType.parse(templateSyntax, builder, this, name, index)
  }

  override fun createPsi(node: ASTNode): PsiElement {
    return XmlASTWrapperPsiElement(node)
  }

  open fun compareTo(other: Angular2EmbeddedExprTokenType): ThreeState =
    if (javaClass != other.javaClass || other.expressionType != expressionType || other.name != name || other.index != index)
      ThreeState.NO
    else
      ThreeState.UNSURE

  class Angular2InterpolationExprTokenType
  internal constructor(templateSyntax: Angular2TemplateSyntax)
    : Angular2EmbeddedExprTokenType(templateSyntax, "NG:INTERPOLATION_EXPR", ExpressionType.INTERPOLATION)

  class Angular2BlockExprTokenType
  internal constructor(templateSyntax: Angular2TemplateSyntax, @NonNls debugName: String, expressionType: ExpressionType, @NonNls blockName: String, parameterIndex: Int)
    : Angular2EmbeddedExprTokenType(templateSyntax, debugName, expressionType, blockName, parameterIndex) {

    val lexerConfig: Angular2Lexer.Config
      get() = Angular2Lexer.BlockParameter(templateSyntax, name!!, index)

    override fun createLexer(): Lexer =
      Angular2Lexer(lexerConfig)

  }

  enum class ExpressionType {
    ACTION {
      override fun parse(templateSyntax: Angular2TemplateSyntax, builder: PsiBuilder, root: IElementType, name: String?, index: Int) {
        Angular2Parser.parseAction(templateSyntax, builder, root)
      }
    },
    BINDING {
      override fun parse(templateSyntax: Angular2TemplateSyntax, builder: PsiBuilder, root: IElementType, name: String?, index: Int) {
        Angular2Parser.parseBinding(templateSyntax, builder, root)
      }
    },
    INTERPOLATION {
      override fun parse(templateSyntax: Angular2TemplateSyntax, builder: PsiBuilder, root: IElementType, name: String?, index: Int) {
        Angular2Parser.parseInterpolation(templateSyntax, builder, root)
      }
    },
    SIMPLE_BINDING {
      override fun parse(templateSyntax: Angular2TemplateSyntax, builder: PsiBuilder, root: IElementType, name: String?, index: Int) {
        Angular2Parser.parseSimpleBinding(templateSyntax, builder, root)
      }
    },
    TEMPLATE_BINDINGS {
      override fun parse(templateSyntax: Angular2TemplateSyntax, builder: PsiBuilder, root: IElementType, name: String?, index: Int) {
        assert(name != null)
        Angular2Parser.parseTemplateBindings(templateSyntax, builder, root, name!!)
      }
    },
    BLOCK_PARAMETER {
      override fun parse(templateSyntax: Angular2TemplateSyntax, builder: PsiBuilder, root: IElementType, name: String?, index: Int) {
        assert(name != null)
        assert(index >= 0)
        Angular2Parser.parseBlockParameter(templateSyntax, builder, root, name!!, index)
      }
    }
    ;

    abstract fun parse(templateSyntax: Angular2TemplateSyntax, builder: PsiBuilder, root: IElementType, name: String?, index: Int)
  }

  companion object {

    @JvmStatic
    fun createActionExpr(templateSyntax: Angular2TemplateSyntax): Angular2EmbeddedExprTokenType =
      Angular2EmbeddedExprTokenType(templateSyntax, "NG:ACTION_EXPR", ExpressionType.ACTION)

    @JvmStatic
    fun createBindingExpr(templateSyntax: Angular2TemplateSyntax): Angular2EmbeddedExprTokenType =
      Angular2EmbeddedExprTokenType(templateSyntax, "NG:BINDING_EXPR", ExpressionType.BINDING)

    @JvmStatic
    fun createInterpolationExpr(templateSyntax: Angular2TemplateSyntax): Angular2EmbeddedExprTokenType =
      Angular2InterpolationExprTokenType(templateSyntax)

    @JvmStatic
    fun createBlockParameter(templateSyntax: Angular2TemplateSyntax, blockName: String, parameterIndex: Int): Angular2EmbeddedExprTokenType =
      Angular2BlockExprTokenType(templateSyntax, "NG:BLOCK_PARAMETER", ExpressionType.BLOCK_PARAMETER, blockName, parameterIndex)

    @JvmStatic
    fun createTemplateBindings(templateSyntax: Angular2TemplateSyntax, templateKey: String): Angular2EmbeddedExprTokenType =
      Angular2EmbeddedExprTokenType(templateSyntax, "NG:TEMPLATE_BINDINGS_EXPR", ExpressionType.TEMPLATE_BINDINGS, templateKey)
  }
}