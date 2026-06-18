// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core

import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode.VueEmbeddedCode

abstract class VueLanguagePlugin(
  val vueCompilerOptions: VueCompilerOptions,
) {
  open fun resolveEmbeddedCode(
    fileName: String,
    ir: IR,
    code: VueEmbeddedCode,
  ) {
  }
}
