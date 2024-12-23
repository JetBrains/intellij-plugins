// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript

import com.intellij.lang.ASTNode
import com.intellij.lang.actionscript.psi.impl.ActionScriptConditionalCompileBlockImpl
import com.intellij.lang.actionscript.psi.impl.ActionScriptGotoStatementImpl
import com.intellij.lang.javascript.JSCompositeElementType
import com.intellij.lang.javascript.types.JSBlockStatementEagerElementType
import com.intellij.psi.tree.IElementType

internal object ActionScriptElementTypes {
  @JvmField
  val GOTO_STATEMENT: IElementType =
    JSCompositeElementType.build("GOTO_STATEMENT", ::ActionScriptGotoStatementImpl)

  @JvmField
  val CONDITIONAL_COMPILE_BLOCK_STATEMENT: IElementType =
    object : JSBlockStatementEagerElementType("CONDITIONAL_COMPILE_BLOCK_STATEMENT") {
      override fun createCompositeNode(): ASTNode {
        return ActionScriptConditionalCompileBlockImpl(this)
      }
    }
}