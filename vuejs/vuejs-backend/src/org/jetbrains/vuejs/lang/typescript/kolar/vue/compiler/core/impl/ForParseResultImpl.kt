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
  override val source: ExpressionNode
    get() = TODO("not implemented")

  override val value: ExpressionNode?
    get() = null // TBD

  override val key: ExpressionNode?
    get() = null  // TBD

  override val index: ExpressionNode?
    get() = null  // TBD
}
