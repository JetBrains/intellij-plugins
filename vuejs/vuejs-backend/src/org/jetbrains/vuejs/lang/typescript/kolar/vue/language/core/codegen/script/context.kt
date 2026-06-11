// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.script

import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.InlayHintInfo
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.LocalTypesGenerator

class ScriptCodegenContext(
  val generatedTypes: MutableSet<String>,
  val localTypes: LocalTypesGenerator,
  val inlayHints: MutableList<InlayHintInfo>,
)

fun createScriptCodegenContext(
  options: ScriptCodegenOptions,
): ScriptCodegenContext =
  ScriptCodegenContext(
    generatedTypes = mutableSetOf(),
    localTypes = LocalTypesGenerator(options.vueCompilerOptions),
    inlayHints = mutableListOf(),
  )
