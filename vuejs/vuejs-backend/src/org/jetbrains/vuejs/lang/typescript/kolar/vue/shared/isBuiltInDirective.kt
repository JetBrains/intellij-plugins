// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.shared

private val builtInDirectives = setOf(
  "bind",
  "cloak",
  "else-if",
  "else",
  "for",
  "html",
  "if",
  "model",
  "on",
  "once",
  "pre",
  "show",
  "slot",
  "text",
  "memo",
)

fun isBuiltInDirective(
  name: String,
): Boolean =
  name in builtInDirectives
