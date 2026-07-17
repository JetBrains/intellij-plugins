// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

const val V_IF: String = "v-if"
const val V_ELSE_IF: String = "v-else-if"
const val V_ELSE: String = "v-else"
const val V_FOR: String = "v-for"

val STRUCTURAL_DIRECTIVE_NAMES: Set<String> =
  setOf(V_IF, V_ELSE_IF, V_ELSE, V_FOR)

private val DIRECTIVE_ATTRIBUTE_STARTS: Set<String> =
  setOf(":", ".", "@", "#", "v-")

fun isDirectiveRawName(
  name: String,
): Boolean =
  DIRECTIVE_ATTRIBUTE_STARTS
    .any { name.startsWith(it) }
