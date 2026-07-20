// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SourceLocation

class SourceLocationImpl(
  override val startOffset: Int,
  override val endOffset: Int,
  override val source: String,
) : SourceLocation {
  companion object {
    val EMPTY: SourceLocation =
      SourceLocationImpl(-1, -1, "")
  }
}

class PsiSourceLocation(
  private val element: PsiElement,
  private val parentRange: TextRange?,
) : SourceLocation {
  private val parentStartOffset: Int
    get() = parentRange?.startOffset ?: 0

  override val startOffset: Int
    get() = parentStartOffset + element.startOffset

  override val endOffset: Int
    get() = parentStartOffset + element.endOffset

  override val source: String
    get() = element.text
}
