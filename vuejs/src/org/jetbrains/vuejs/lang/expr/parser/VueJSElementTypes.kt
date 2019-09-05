// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.types.JSVariableElementType
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.ICompositeElementType
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.psi.impl.*

object VueJSElementTypes {

  val V_FOR_EXPRESSION: IElementType = object : VueJSCompositeElementType("V_FOR_EXPRESSION") {
    override fun createCompositeNode(): ASTNode = VueJSVForExpressionImpl(this)
  }

  val SLOT_PROPS_EXPRESSION: IElementType = object : VueJSCompositeElementType("SLOT_PROPS_EXPRESSION") {
    override fun createCompositeNode(): ASTNode = VueJSSlotPropsExpressionImpl(this)
  }

  val V_FOR_VARIABLE: JSVariableElementType = object : JSVariableElementType("V_FOR_VARIABLE") {
    override fun construct(node: ASTNode?): PsiElement? {
      return VueJSVForVariableImpl(node)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
      return false
    }
  }

  val SLOT_PROPS_VARIABLE: JSVariableElementType = object : JSVariableElementType("SLOT_PROPS_VARIABLE") {
    override fun construct(node: ASTNode?): PsiElement? {
      return VueJSSlotPropsVariableImpl(node)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
      return false
    }
  }

  val EMBEDDED_EXPR_STATEMENT: IElementType = object : VueJSCompositeElementType("VUE:EMBEDDED_EXPR_STATEMENT") {
    override fun createCompositeNode(): ASTNode = VueJSEmbeddedExpressionImpl(this)
  }

  private abstract class VueJSCompositeElementType(debugName: String)
    : IElementType(debugName, VueJSLanguage.INSTANCE), ICompositeElementType
}

