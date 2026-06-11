// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.script

import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRScript
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRScriptSetup
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.ScriptRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.ScriptSetupRanges

data class ScriptCodegenOptions(
  val vueCompilerOptions: VueCompilerOptions,
  val script: IRScript?,
  val scriptSetup: IRScriptSetup?,
  val fileName: String,
  val scriptRanges: ScriptRanges?,
  val scriptSetupRanges: ScriptSetupRanges?,
  val templateAndStyleTypes: Set<String>,
  val templateAndStyleCodes: List<Code>,
  val exposed: Set<String>,
)
