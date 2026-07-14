// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

private val DIRECTIVE_ATTRIBUTE_STARTS: Set<String> =
  setOf(":", ".", "@", "#", "v-")

fun isDirectiveAttributeName(
  name: String,
): Boolean =
  DIRECTIVE_ATTRIBUTE_STARTS
    .any { name.startsWith(it) }
