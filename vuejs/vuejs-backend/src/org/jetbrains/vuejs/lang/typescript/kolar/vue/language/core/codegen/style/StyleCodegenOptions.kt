// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.style

import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRStyle

data class StyleCodegenOptions(
  val vueCompilerOptions: VueCompilerOptions,
  val styles: List<IRStyle>,
  val setupRefs: Set<String>,
  val setupConsts: Set<String>,
)
