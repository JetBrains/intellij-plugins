// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core

import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset

interface SourceLocation {
  val startOffset: Int
  val endOffset: Int
  val source: String
}

class SourceLocationImpl(
  override val startOffset: Int,
  override val endOffset: Int,
  override val source: String,
) : SourceLocation

class PsiSourceLocation(
  private val element: PsiElement,
) : SourceLocation {
  override val startOffset: Int
    get() = element.startOffset

  override val endOffset: Int
    get() = element.endOffset

  override val source: String
    get() = element.text
}
