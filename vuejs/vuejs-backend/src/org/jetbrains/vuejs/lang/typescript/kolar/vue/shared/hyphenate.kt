// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.shared

private val hyphenateRegex = Regex("""\B([A-Z])""")

fun hyphenate(
  str: String,
): String =
  hyphenateRegex.replace(str) { "-${it.groupValues[1]}" }.lowercase()
