// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar

import org.jetbrains.vuejs.config.VueCompilerOptions

internal object VueCompilerOptionsHelper {
  fun getTypesRoot(): String {
    if (VueTranspilerTestMixin.useDefaultTypesRoot)
      return VueCompilerOptions.DEFAULT_TYPES_ROOT

    return VueCompilerOptions.DEFAULT_TYPES_ROOT
  }
}