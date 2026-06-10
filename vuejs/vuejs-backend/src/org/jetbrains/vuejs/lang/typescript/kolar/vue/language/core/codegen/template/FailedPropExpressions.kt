// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode

data class FailedPropExpressions(
  val node: SimpleExpressionNode,
  val prefix: String,
  val suffix: String,
)
