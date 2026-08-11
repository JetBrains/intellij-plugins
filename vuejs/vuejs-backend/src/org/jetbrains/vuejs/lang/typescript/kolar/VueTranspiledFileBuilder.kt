// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findPsiFile
import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.toString
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueLanguagePlugin
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueMapping
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.plugins.VueTsxPlugin
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode.VueEmbeddedCode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode.VueEmbeddedCode.Companion.SCRIPT_ID
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode.getMappingsForCode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode.useIR

@Service(Service.Level.APP)
class VueTranspiledFileBuilder {
  private val plugin: VueLanguagePlugin =
    VueTsxPlugin(VueCompilerOptions())

  fun getTranspiledFile(
    project: Project,
    file: VirtualFile,
  ): TranspiledFile? {
    return file.findPsiFile(project)
      ?.let { it as VueFile }
      ?.let { getTranspiledFile(it) }
  }

  private fun getTranspiledFile(
    file: VueFile,
  ): TranspiledFile {
    val code = VueEmbeddedCode(
      id = SCRIPT_ID,
      lang = "ts",
      content = emptyList(),
    )

    plugin.resolveEmbeddedCode(fileName = file.name, useIR(file), code)

    val generatedCode = toString(code.content)
    val mappings = getMappingsForCode(code)

    return TranspiledFile(
      generatedCode = generatedCode,
      mappings = mappings,
    )
  }

  class TranspiledFile(
    val generatedCode: String,
    val mappings: List<VueMapping>,
  )

  companion object {
    fun getInstance(): VueTranspiledFileBuilder = service()
  }
}
