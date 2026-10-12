// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.gist.GistManager
import com.intellij.util.gist.PsiFileGist
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.IOUtil
import kotlinx.serialization.json.Json
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
import java.io.DataInput
import java.io.DataOutput

private const val TRANSPILED_FILE_GIST_VERSION: Int = 5

@Service(Service.Level.APP)
class VueTranspiledFileBuilder {
  private val plugin: VueLanguagePlugin =
    VueTsxPlugin(VueCompilerOptions())

  private val transpiledFileGist: PsiFileGist<TranspiledFile> =
    GistManager.getInstance().newPsiFileGist(
      "vue-transpiled-file",
      TRANSPILED_FILE_GIST_VERSION,
      TranspiledFileExternalizer,
    ) { file ->
      (file as? VueFile)?.let { getTranspiledFileInternal(it) }
    }

  fun getTranspiledFile(
    file: VueFile,
  ): TranspiledFile? =
    transpiledFileGist.getFileData(file)

  private fun getTranspiledFileInternal(
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

  private object TranspiledFileExternalizer :
    DataExternalizer<TranspiledFile> {

    override fun save(out: DataOutput, value: TranspiledFile) {
      IOUtil.writeUTF(out, value.generatedCode)
      IOUtil.writeUTF(out, Json.encodeToString(value.mappings))
    }

    override fun read(`in`: DataInput): TranspiledFile =
      TranspiledFile(
        generatedCode = IOUtil.readUTF(`in`),
        mappings = Json.decodeFromString(IOUtil.readUTF(`in`)),
      )
  }

  companion object {
    fun getInstance(): VueTranspiledFileBuilder = service()
  }
}
