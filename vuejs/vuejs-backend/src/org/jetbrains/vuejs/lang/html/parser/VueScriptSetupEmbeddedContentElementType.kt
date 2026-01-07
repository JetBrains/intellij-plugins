// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.lang.html.psi.impl.VueScriptSetupEmbeddedContentImpl

class VueScriptSetupEmbeddedContentElementType(
  forcedLanguage: JSLanguageDialect,
  debugName: String,
) : JSEmbeddedContentElementType(forcedLanguage, debugName) {

  override fun toString(): String =
    "VUE:$debugName"

  override fun construct(node: ASTNode): PsiElement {
    return VueScriptSetupEmbeddedContentImpl(node)
  }

  override fun isModule(): Boolean =
    true

}