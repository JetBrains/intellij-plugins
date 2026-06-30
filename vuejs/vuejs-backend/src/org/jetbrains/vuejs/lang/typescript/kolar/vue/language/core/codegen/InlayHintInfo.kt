// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen

import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source

data class InlayHintInfo(
  val blockName: Source,
  val offset: Int,
  val setting: String,
  val label: String,
  val tooltip: String? = null,
  val paddingRight: Boolean = false,
  val paddingLeft: Boolean = false,
)
