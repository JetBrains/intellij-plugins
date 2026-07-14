// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core

interface SourceLocation {
  val startOffset: Int
  val endOffset: Int
  val source: String
}
