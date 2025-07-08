// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSElementType
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.expr.psi.impl.VueJSEmbeddedExpressionContentImpl

class VueJSEmbeddedExpressionContentElementType(debugName: String, private val language: Language)
  : JSElementType<VueJSEmbeddedExpressionContent>(debugName) {

  override fun getLanguage(): Language = language

  override fun construct(node: ASTNode): PsiElement {
    return VueJSEmbeddedExpressionContentImpl(node)
  }

  override fun toString(): String = VueJSElementTypes.EXTERNAL_ID_PREFIX + debugName
}
