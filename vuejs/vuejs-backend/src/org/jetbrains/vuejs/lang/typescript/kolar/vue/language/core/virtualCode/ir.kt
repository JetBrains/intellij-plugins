// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode

import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IR
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRScript
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRScriptSetup
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRTemplate

fun useIR(
  file: VueFile,
): IR {
  return IR(
    template = getTemplate(file),
    script = getScript(file),
    scriptSetup = getScriptSetup(file),
    styles = emptyList(),
  )
}

private fun getTemplate(
  file: VueFile,
): IRTemplate? {
  TODO()
}

private fun getScript(
  file: VueFile,
): IRScript? {
  TODO()
}

private fun getScriptSetup(
  file: VueFile,
): IRScriptSetup? {
  TODO()
}
