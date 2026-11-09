// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.common

import org.jetbrains.vuejs.config.VueCompilerOptions

interface CommonCodegenOptions {
  val vueCompilerOptions: VueCompilerOptions
  val destructuredProps: Set<String>
  val importedComponents: Set<String>
  val setupRefs: Set<String>
  val setupBindings: Set<String>
  val dotValueBindings: Set<String>
}
