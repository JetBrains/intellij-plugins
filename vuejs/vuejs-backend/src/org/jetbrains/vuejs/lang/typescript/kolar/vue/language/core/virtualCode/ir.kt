// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode

import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IR

fun useIR(
  file: VueFile,
): IR {
  return IR(
    template = null,
    script = null,
    scriptSetup = null,
    styles = emptyList(),
  )
}
