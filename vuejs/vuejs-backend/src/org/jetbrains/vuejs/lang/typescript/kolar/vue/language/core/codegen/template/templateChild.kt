// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code

fun generateTemplateChild(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: Node, // CompilerDOM.RootNode | CompilerDOM.TemplateChildNode | CompilerDOM.SimpleExpressionNode
  enterNode: Boolean = true,
  treatTemplateAsFragment: Boolean = false,
): Sequence<Code> = 
  TODO()
