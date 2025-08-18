// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript

import com.intellij.lang.ASTNode
import com.intellij.lang.actionscript.psi.impl.ActionScriptConditionalCompileBlockImpl
import com.intellij.lang.actionscript.psi.impl.ActionScriptGotoStatementImpl
import com.intellij.lang.actionscript.psi.impl.JSE4XFilterQueryArgumentListImpl
import com.intellij.lang.javascript.JSCompositeElementType
import com.intellij.lang.javascript.types.JSArgumentListElementType
import com.intellij.lang.javascript.types.JSBlockStatementEagerElementType
import com.intellij.psi.tree.IElementType

internal object ActionScriptInternalElementTypes {
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

  @JvmField
  val E4X_FILTER_QUERY_ARGUMENT_LIST: IElementType =
    JSArgumentListElementType("E4X_FILTER_QUERY_ARGUMENT_LIST") {
      JSE4XFilterQueryArgumentListImpl()
    }
}