// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueMapping

object VueTranspiledFileBuilder {
  fun getTranspiledFile(
    file: PsiFile,
  ): TranspiledFile? {
    // TBD
    return null
  }

  class TranspiledFile(
    val generatedCode: String,
    val mappings: List<VueMapping>,
  )
}
