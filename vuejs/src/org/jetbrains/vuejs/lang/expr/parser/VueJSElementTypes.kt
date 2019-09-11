// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.ICompositeElementType
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.psi.impl.VueJSEmbeddedExpressionImpl
import org.jetbrains.vuejs.lang.expr.psi.impl.VueJSSlotPropsExpressionImpl
import org.jetbrains.vuejs.lang.expr.psi.impl.VueJSVForExpressionImpl

object VueJSElementTypes {

  val V_FOR_EXPRESSION: IElementType = object : VueJSCompositeElementType("V_FOR_EXPRESSION") {
    override fun createCompositeNode(): ASTNode = VueJSVForExpressionImpl(this)
  }

  val SLOT_PROPS_EXPRESSION: IElementType = object : VueJSCompositeElementType("SLOT_PROPS_EXPRESSION") {
    override fun createCompositeNode(): ASTNode = VueJSSlotPropsExpressionImpl(this)
  }

  val EMBEDDED_EXPR_STATEMENT: IElementType = object : VueJSCompositeElementType("VUE:EMBEDDED_EXPR_STATEMENT") {
    override fun createCompositeNode(): ASTNode = VueJSEmbeddedExpressionImpl(this)
  }

  private abstract class VueJSCompositeElementType(debugName: String)
    : IElementType(debugName, VueJSLanguage.INSTANCE), ICompositeElementType
}

