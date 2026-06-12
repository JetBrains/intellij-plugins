// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen

import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SourceLocation

fun createVBindShorthandInlayHintInfo(
  loc: SourceLocation,
  variableName: String,
): InlayHintInfo =
  InlayHintInfo(
    blockName = "template",
    offset = loc.end.offset,
    setting = "vue.inlayHints.vBindShorthand",
    label = "=\"$variableName\"",
    tooltip = listOf(
      "This is a shorthand for `${loc.source}=\"$variableName\"`.",
      "To hide this hint, set `vue.inlayHints.vBindShorthand` to `false` in IDE settings.",
      "[More info](https://github.com/vuejs/core/pull/9451)",
    ).joinToString("\n\n"),
  )
