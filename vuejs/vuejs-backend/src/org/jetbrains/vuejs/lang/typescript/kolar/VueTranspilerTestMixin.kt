// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import org.jetbrains.annotations.TestOnly

object VueTranspilerTestMixin {
  private const val USE_DEFAULT_TYPES_ROOT = "vue.transpiler.use.default.types.root"

  val useDefaultTypesRoot: Boolean
    get() = ApplicationManager.getApplication().isUnitTestMode
            && System.getProperty(USE_DEFAULT_TYPES_ROOT) == "true"

  @TestOnly
  fun setUseDefaultTypesRoot(
    value: Boolean,
    disposable: Disposable,
  ) {
    if (!value) {
      System.clearProperty(USE_DEFAULT_TYPES_ROOT)
      return
    }

    System.setProperty(USE_DEFAULT_TYPES_ROOT, "true")

    Disposer.register(disposable) {
      System.clearProperty(USE_DEFAULT_TYPES_ROOT)
    }
  }
}
