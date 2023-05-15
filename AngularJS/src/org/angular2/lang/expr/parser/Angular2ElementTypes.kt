// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSKeywordSets.IDENTIFIER_NAMES
import com.intellij.lang.javascript.JSTokenTypes.STRING_LITERAL
import com.intellij.lang.javascript.types.JSExpressionElementType
import com.intellij.psi.tree.ICompositeElementType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.impl.*
import org.jetbrains.annotations.NonNls
import java.util.function.Function

interface Angular2ElementTypes : JSElementTypes, Angular2StubElementTypes {

  open class Angular2ElementType(@NonNls debugName: String,
                                 private val myClassConstructor: Function<Angular2ElementType, ASTNode>)
    : IElementType(debugName, Angular2Language.INSTANCE), ICompositeElementType {

    override fun createCompositeNode(): ASTNode {
      return myClassConstructor.apply(this)
    }
  }

  class Angular2ExpressionElementType(@NonNls debugName: String,
                                      classConstructor: Function<Angular2ElementType, ASTNode>)
    : Angular2ElementType(debugName, classConstructor), JSExpressionElementType

  class Angular2TemplateBindingType(private val myKey: String, private val myVar: Boolean, private val myName: String?)
    : IElementType("NG:TEMPLATE_BINDING_STATEMENT", Angular2Language.INSTANCE, false), ICompositeElementType {

    override fun createCompositeNode(): ASTNode {
      return Angular2TemplateBindingImpl(TEMPLATE_BINDING_STATEMENT, myKey, myVar, myName)
    }
  }

  class Angular2TemplateBindingsType(private val myTemplateName: String)
    : IElementType("NG:TEMPLATE_BINDINGS_STATEMENT", Angular2Language.INSTANCE, false), ICompositeElementType {

    override fun createCompositeNode(): ASTNode {
      return Angular2TemplateBindingsImpl(TEMPLATE_BINDINGS_STATEMENT, myTemplateName)
    }
  }

  companion object {
    @JvmField
    val PIPE_EXPRESSION: IElementType =
      Angular2ExpressionElementType("NG:PIPE_EXPRESSION") { node -> Angular2PipeExpressionImpl(node) }

    @JvmField
    val PIPE_ARGUMENTS_LIST: IElementType =
      Angular2ExpressionElementType("NG:PIPE_ARGUMENTS_LIST") { node -> Angular2PipeArgumentsListImpl(node) }

    @JvmField
    val PIPE_LEFT_SIDE_ARGUMENT: IElementType =
      Angular2ExpressionElementType("NG:PIPE_LEFT_SIDE_ARGUMENT") { node -> Angular2PipeLeftSideArgumentImpl(node) }

    @JvmField
    val PIPE_REFERENCE_EXPRESSION: IElementType =
      Angular2ExpressionElementType("NG:PIPE_REFERENCE_EXPRESSION") { node -> Angular2PipeReferenceExpressionImpl(node) }

    @JvmField
    val CHAIN_STATEMENT: IElementType =
      Angular2ElementType("NG:CHAIN_STATEMENT") { node -> Angular2ChainImpl(node) }

    @JvmField
    val QUOTE_STATEMENT: IElementType =
      Angular2ElementType("NG:QUOTE_STATEMENT") { node -> Angular2QuoteImpl(node) }

    @JvmField
    val ACTION_STATEMENT: IElementType =
      Angular2ElementType("NG:ACTION") { node -> Angular2ActionImpl(node) }

    @JvmField
    val BINDING_STATEMENT: IElementType =
      Angular2ElementType("NG:BINDING") { node -> Angular2BindingImpl(node) }

    @JvmField
    val INTERPOLATION_STATEMENT: IElementType =
      Angular2ElementType("NG:INTERPOLATION") { node -> Angular2InterpolationImpl(node) }

    @JvmField
    val SIMPLE_BINDING_STATEMENT: IElementType =
      Angular2ElementType("NG:SIMPLE_BINDING") { node -> Angular2SimpleBindingImpl(node) }

    @JvmField
    val TEMPLATE_BINDINGS_STATEMENT: IElementType =
      Angular2ElementType("NG:TEMPLATE_BINDINGS_STATEMENT") {
        throw UnsupportedOperationException("Use createTemplateBindingsStatement method instead")
      }

    @JvmField
    val TEMPLATE_BINDING_KEY: IElementType =
      Angular2ElementType("NG:TEMPLATE_BINDING_KEY") { node -> Angular2TemplateBindingKeyImpl(node) }

    @JvmField
    val TEMPLATE_BINDING_STATEMENT: IElementType =
      Angular2ElementType("NG:TEMPLATE_BINDING_STATEMENT") {
        throw UnsupportedOperationException("Use createTemplateBindingStatement method instead")
      }

    @JvmField
    val PROPERTY_NAMES = TokenSet.orSet(IDENTIFIER_NAMES, TokenSet.create(STRING_LITERAL))

    @JvmStatic
    fun createTemplateBindingStatement(key: String, isVar: Boolean, name: String?): IElementType {
      return Angular2TemplateBindingType(key, isVar, name)
    }

    @JvmStatic
    fun createTemplateBindingsStatement(templateName: String): IElementType {
      return Angular2TemplateBindingsType(templateName)
    }
  }
}
