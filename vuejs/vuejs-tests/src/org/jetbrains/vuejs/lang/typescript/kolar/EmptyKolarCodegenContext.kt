// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.lang.typescript.kolar.KolarCodegenContext
import com.intellij.lang.typescript.kolar.KolarSourceScript
import com.intellij.lang.typescript.kolar.ScriptId

internal object EmptyKolarCodegenContext:
  KolarCodegenContext  {
  override fun getAssociatedScript(scriptId: ScriptId): KolarSourceScript? = null
  override fun normalizePath(path: String): String = path
}
