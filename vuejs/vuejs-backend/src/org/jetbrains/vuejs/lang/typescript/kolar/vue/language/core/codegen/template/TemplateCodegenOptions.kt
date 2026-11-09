// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRTemplate
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.common.CommonCodegenOptions

data class TemplateCodegenOptions(
  override val vueCompilerOptions: VueCompilerOptions,
  val template: IRTemplate,
  val isVapor: Boolean,
  override val destructuredProps: Set<String>,
  override val importedComponents: Set<String>,
  override val setupRefs: Set<String>,
  override val setupBindings: Set<String>,
  // Bindings narrowed at least once anywhere in the template/styles; every
  // access of these is emitted with `.value`. Collected by a first codegen pass.
  override val dotValueBindings: Set<String>,
  // The subset of `dotValueBindings` re-asserted at closure tops (imports, `let`/`var`).
  val reassertBindings: Set<String>,
  val hasDefineSlots: Boolean = false,
  val propsAssignName: String? = null,
  val slotsAssignName: String? = null,
  val inheritAttrs: Boolean,
  val componentName: String,
) : CommonCodegenOptions
