// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRTemplate

data class TemplateCodegenOptions(
  val vueCompilerOptions: VueCompilerOptions,
  val template: IRTemplate,
  override val setupRefs: Set<String>,
  val setupConsts: Set<String>,
  val hasDefineSlots: Boolean = false,
  val propsAssignName: String? = null,
  val slotsAssignName: String? = null,
  val inheritAttrs: Boolean,
  val componentName: String,
) : HasSetupRefs
