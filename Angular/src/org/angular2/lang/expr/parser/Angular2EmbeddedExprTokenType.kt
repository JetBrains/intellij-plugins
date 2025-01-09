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
import org.jetbrains.annotations.NonNls
import java.util.*

open class Angular2EmbeddedExprTokenType : HtmlCustomEmbeddedContentTokenType {

  protected val expressionType: ExpressionType
  protected val name: String?
  protected val index: Int

  private constructor(@NonNls debugName: String, expressionType: ExpressionType)
    : super(debugName, Angular2Language) {
    this.expressionType = expressionType
    name = null
    index = -1
  }

  private constructor(@NonNls debugName: String, expressionType: ExpressionType, @NonNls templateKey: String?)
    : super("$debugName ($templateKey)", Angular2Language, false) {
    this.expressionType = expressionType
    name = templateKey
    index = -1
  }

  private constructor(@NonNls debugName: String, expressionType: ExpressionType, @NonNls blockName: String?, parameterIndex: Int)
    : super("$debugName ($blockName:$parameterIndex)", Angular2Language, false) {
    this.expressionType = expressionType
    name = blockName
    index = parameterIndex
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val type = other as Angular2EmbeddedExprTokenType?
    return expressionType == type!!.expressionType && name == type.name
  }

  override fun hashCode(): Int {
    return Objects.hash(super.hashCode(), expressionType, name)
  }

  override fun createLexer(): Lexer =
    Angular2Lexer(Angular2Lexer.RegularBinding)

  override fun parse(builder: PsiBuilder) {
    expressionType.parse(builder, this, name, index)
  }

  override fun createPsi(node: ASTNode): PsiElement {
    return XmlASTWrapperPsiElement(node)
  }

  open fun compareTo(other: Angular2EmbeddedExprTokenType): ThreeState =
    if (javaClass != other.javaClass || other.expressionType != expressionType || other.name != name || other.index != index)
      ThreeState.NO
    else
      ThreeState.UNSURE

  class Angular2BlockExprTokenType
  internal constructor(@NonNls debugName: String, expressionType: ExpressionType, @NonNls blockName: String, parameterIndex: Int)
    : Angular2EmbeddedExprTokenType(debugName, expressionType, blockName, parameterIndex) {

    val lexerConfig: Angular2Lexer.Config
      get() = Angular2Lexer.BlockParameter(name!!, index)

    override fun createLexer(): Lexer =
      Angular2Lexer(lexerConfig)

  }

  enum class ExpressionType {
    ACTION {
      override fun parse(builder: PsiBuilder, root: IElementType, name: String?, index: Int) {
        Angular2Parser.parseAction(builder, root)
      }
    },
    BINDING {
      override fun parse(builder: PsiBuilder, root: IElementType, name: String?, index: Int) {
        Angular2Parser.parseBinding(builder, root)
      }
    },
    INTERPOLATION {
      override fun parse(builder: PsiBuilder, root: IElementType, name: String?, index: Int) {
        Angular2Parser.parseInterpolation(builder, root)
      }
    },
    SIMPLE_BINDING {
      override fun parse(builder: PsiBuilder, root: IElementType, name: String?, index: Int) {
        Angular2Parser.parseSimpleBinding(builder, root)
      }
    },
    TEMPLATE_BINDINGS {
      override fun parse(builder: PsiBuilder, root: IElementType, name: String?, index: Int) {
        assert(name != null)
        Angular2Parser.parseTemplateBindings(builder, root, name!!)
      }
    },
    BLOCK_PARAMETER {
      override fun parse(builder: PsiBuilder, root: IElementType, name: String?, index: Int) {
        assert(name != null)
        assert(index >= 0)
        Angular2Parser.parseBlockParameter(builder, root, name!!, index)
      }
    }
    ;

    abstract fun parse(builder: PsiBuilder, root: IElementType, name: String?, index: Int)
  }

  companion object {

    @JvmField
    val ACTION_EXPR: Angular2EmbeddedExprTokenType = Angular2EmbeddedExprTokenType(
      "NG:ACTION_EXPR", ExpressionType.ACTION)

    @JvmField
    val BINDING_EXPR: Angular2EmbeddedExprTokenType = Angular2EmbeddedExprTokenType(
      "NG:BINDING_EXPR", ExpressionType.BINDING)

    @JvmField
    val INTERPOLATION_EXPR: Angular2EmbeddedExprTokenType = Angular2EmbeddedExprTokenType(
      "NG:INTERPOLATION_EXPR", ExpressionType.INTERPOLATION)

    @JvmField
    val SIMPLE_BINDING_EXPR: Angular2EmbeddedExprTokenType = Angular2EmbeddedExprTokenType(
      "NG:SIMPLE_BINDING_EXPR", ExpressionType.SIMPLE_BINDING)

    @JvmStatic
    fun createBlockParameter(blockName: String, parameterIndex: Int): Angular2EmbeddedExprTokenType {
      return Angular2BlockExprTokenType("NG:BLOCK_PARAMETER", ExpressionType.BLOCK_PARAMETER, blockName, parameterIndex)
    }

    @JvmStatic
    fun createTemplateBindings(templateKey: String): Angular2EmbeddedExprTokenType {
      return Angular2EmbeddedExprTokenType("NG:TEMPLATE_BINDINGS_EXPR", ExpressionType.TEMPLATE_BINDINGS, templateKey)
    }
  }
}