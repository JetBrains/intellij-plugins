// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SourceLocation

open class NodeImpl
protected constructor(
  private val element: PsiElement,
  private val parentRange: TextRange? = null,
) : Node {
  final override val loc: SourceLocation by lazy {
    PsiSourceLocation(element, parentRange)
  }
}
