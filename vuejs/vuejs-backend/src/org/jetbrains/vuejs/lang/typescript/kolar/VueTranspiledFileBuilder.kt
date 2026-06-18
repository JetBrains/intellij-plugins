// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.toString
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IR
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueLanguagePlugin
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueMapping
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.plugins.VueTsxPlugin
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode.VueEmbeddedCode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode.VueEmbeddedCode.Companion.SCRIPT_ID
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode.getMappingsForCode

object VueTranspiledFileBuilder {
  private val plugin: VueLanguagePlugin =
    VueTsxPlugin(VueCompilerOptions())

  fun getTranspiledFile(
    file: PsiFile,
  ): TranspiledFile {
    val code = VueEmbeddedCode(
      id = SCRIPT_ID,
      lang = "ts",
      content = emptyList(),
    )

    plugin.resolveEmbeddedCode(fileName = file.name, ir(file), code)

    val generatedCode = toString(code.content)
    val mappings = getMappingsForCode(code, emptyMap())

    return TranspiledFile(
      generatedCode = generatedCode,
      mappings = mappings,
    )
  }

  private fun ir(
    file: PsiFile,
  ): IR =
    IR(
      content = file.text,
      comments = emptyList(),
      template = null,
      script = null,
      scriptSetup = null,
      styles = emptyList(),
      customBlocks = emptyList(),
    )

  class TranspiledFile(
    val generatedCode: String,
    val mappings: List<VueMapping>,
  )
}
