// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSKeywordSets.IDENTIFIER_NAMES
import com.intellij.lang.javascript.JSTokenTypes.STRING_LITERAL
import com.intellij.lang.javascript.psi.JSElementType
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.types.JSExpressionElementType
import com.intellij.psi.tree.ICompositeElementType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.impl.*
import org.jetbrains.annotations.NonNls
import java.util.function.Function

object Angular2ElementTypes {

  open class Angular2ElementType(@NonNls debugName: String,
                                 private val myClassConstructor: Function<Angular2ElementType, ASTNode>)
    : IElementType(debugName, Angular2Language), ICompositeElementType {

    override fun createCompositeNode(): ASTNode {
      return myClassConstructor.apply(this)
    }
  }

  class Angular2ExpressionElementType(@NonNls debugName: String,
                                      classConstructor: Function<Angular2ElementType, ASTNode>)
    : Angular2ElementType(debugName, classConstructor), JSExpressionElementType

  const val EXTERNAL_ID_PREFIX: String = "NG:"

  val TEMPLATE_VARIABLE: JSElementType<JSVariable> = Angular2TemplateVariableElementType()

  val BLOCK_PARAMETER_VARIABLE: JSElementType<JSVariable> = Angular2BlockParameterVariableElementType()

  val STRING_PARTS_LITERAL_EXPRESSION: JSElementType<JSLiteralExpression> = Angular2StringPartsLiteralExpressionType()

  val DEFERRED_TIME_LITERAL_EXPRESSION: Angular2DeferredTimeLiteralExpressionElementType = Angular2DeferredTimeLiteralExpressionElementType()

  class Angular2TemplateBindingType(private val key: String, private val keyKind: Angular2TemplateBinding.KeyKind, private val name: String?)
    : IElementType("NG:TEMPLATE_BINDING_STATEMENT", Angular2Language, false), ICompositeElementType {

    override fun createCompositeNode(): ASTNode {
      return Angular2TemplateBindingImpl(TEMPLATE_BINDING_STATEMENT, key, keyKind, name)
    }
  }

  class Angular2TemplateBindingsType(private val myTemplateName: String)
    : IElementType("NG:TEMPLATE_BINDINGS_STATEMENT", Angular2Language, false), ICompositeElementType {

    override fun createCompositeNode(): ASTNode {
      return Angular2TemplateBindingsImpl(TEMPLATE_BINDINGS_STATEMENT, myTemplateName)
    }
  }

  val PIPE_EXPRESSION: IElementType =
    Angular2ExpressionElementType("NG:PIPE_EXPRESSION") { node -> Angular2PipeExpressionImpl(node) }

  val PIPE_ARGUMENTS_LIST: IElementType =
    Angular2ExpressionElementType("NG:PIPE_ARGUMENTS_LIST") { node -> Angular2PipeArgumentsListImpl(node) }

  val PIPE_LEFT_SIDE_ARGUMENT: IElementType =
    Angular2ExpressionElementType("NG:PIPE_LEFT_SIDE_ARGUMENT") { node -> Angular2PipeLeftSideArgumentImpl(node) }

  val PIPE_REFERENCE_EXPRESSION: IElementType =
    Angular2ExpressionElementType("NG:PIPE_REFERENCE_EXPRESSION") { node -> Angular2PipeReferenceExpressionImpl(node) }

  val CHAIN_STATEMENT: IElementType =
    Angular2ElementType("NG:CHAIN_STATEMENT") { node -> Angular2ChainImpl(node) }

  val QUOTE_STATEMENT: IElementType =
    Angular2ElementType("NG:QUOTE_STATEMENT") { node -> Angular2QuoteImpl(node) }

  val ACTION_STATEMENT: IElementType =
    Angular2ElementType("NG:ACTION") { node -> Angular2ActionImpl(node) }

  val BINDING_STATEMENT: IElementType =
    Angular2ElementType("NG:BINDING") { node -> Angular2BindingImpl(node) }

  val INTERPOLATION_STATEMENT: IElementType =
    Angular2ElementType("NG:INTERPOLATION") { node -> Angular2InterpolationImpl(node) }

  val SIMPLE_BINDING_STATEMENT: IElementType =
    Angular2ElementType("NG:SIMPLE_BINDING") { node -> Angular2SimpleBindingImpl(node) }

  val TEMPLATE_BINDINGS_STATEMENT: IElementType =
    Angular2ElementType("NG:TEMPLATE_BINDINGS_STATEMENT") {
      throw UnsupportedOperationException("Use createTemplateBindingsStatement method instead")
    }

  val TEMPLATE_BINDING_KEY: IElementType =
    Angular2ElementType("NG:TEMPLATE_BINDING_KEY") { node -> Angular2TemplateBindingKeyImpl(node) }

  val TEMPLATE_BINDING_STATEMENT: IElementType =
    Angular2ElementType("NG:TEMPLATE_BINDING_STATEMENT") {
      throw UnsupportedOperationException("Use createTemplateBindingStatement method instead")
    }

  val BLOCK_PARAMETER_STATEMENT: IElementType =
    Angular2ElementType("NG:BLOCK_PARAMETER_STATEMENT") { node -> Angular2BlockParameterImpl(node) }

  val PROPERTY_NAMES: TokenSet = TokenSet.orSet(IDENTIFIER_NAMES, TokenSet.create(STRING_LITERAL))

  @JvmStatic
  fun createTemplateBindingStatement(key: String, keyKind: Angular2TemplateBinding.KeyKind, name: String?): IElementType {
    return Angular2TemplateBindingType(key, keyKind, name)
  }

  @JvmStatic
  fun createTemplateBindingsStatement(templateName: String): IElementType {
    return Angular2TemplateBindingsType(templateName)
  }
}
