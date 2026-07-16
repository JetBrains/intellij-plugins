// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSVarStatement
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ForParseResult

class ForParseResultImpl(
  private val varStatement: JSVarStatement?,
  private val collectionExpression: JSExpression,
) : ForParseResult {
  private val variableNodeMap: MutableMap<Int, ExpressionNode?> = mutableMapOf()

  private fun variableNode(
    index: Int,
  ): ExpressionNode? {
    if (!variableNodeMap.containsKey(index)) {
      variableNodeMap[index] = varStatement
        ?.declarations
        ?.getOrNull(index)
        ?.let(::SimpleExpressionNodeImpl)
    }

    return variableNodeMap[index]
  }

  override val source: ExpressionNode by lazy {
    SimpleExpressionNodeImpl(collectionExpression)
  }

  override val value: ExpressionNode?
    get() = variableNode(0)

  override val key: ExpressionNode?
    get() = variableNode(1)

  override val index: ExpressionNode?
    get() = variableNode(2)
}
