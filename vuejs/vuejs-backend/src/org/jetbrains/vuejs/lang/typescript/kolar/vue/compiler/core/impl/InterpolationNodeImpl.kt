// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.openapi.util.TextRange
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.InterpolationNode

class InterpolationNodeImpl(
  private val expressionContent: VueJSEmbeddedExpressionContent,
  private val parentRange: TextRange,
) : NodeImpl(expressionContent),
    InterpolationNode {
  override val content: ExpressionNode by lazy {
    // TODO: support compound expressions
    SimpleExpressionNodeImpl(expressionContent)
  }

  companion object {
    fun sourceLocationWithWhitespaces(
      node: InterpolationNode,
    ): Pair<String, Int> {
      node as InterpolationNodeImpl

      return Pair(
        node.expressionContent.parent.text,
        node.parentRange.startOffset,
      )
    }
  }
}
