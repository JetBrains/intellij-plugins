// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.style

import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRStyle
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.common.CommonCodegenOptions

data class StyleCodegenOptions(
  override val vueCompilerOptions: VueCompilerOptions,
  val styles: List<IRStyle>,
  override val destructuredProps: Set<String>,
  override val importedComponents: Set<String>,
  override val setupRefs: Set<String>,
  override val setupBindings: Set<String>,
  override val dotValueBindings: Set<String>,
) : CommonCodegenOptions
