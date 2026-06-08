// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

const val newLine: String = "\n"
const val endOfLine: String = ";\n"
val identifierRegex: Regex = Regex("^[a-zA-Z_\$][0-9a-zA-Z_\$]*\$")
