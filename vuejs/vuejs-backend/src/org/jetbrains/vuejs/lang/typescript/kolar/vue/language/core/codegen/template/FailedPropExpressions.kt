// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

data class FailedPropExpressions(
  val node: Any,  // CompilerDOM.SimpleExpressionNode
  val prefix: String,
  val suffix: String,
)
