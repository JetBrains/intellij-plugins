package org.angular2.lang.expr.service.protocol.commands

import com.intellij.lang.javascript.service.protocol.LocalFilePath
import com.intellij.lang.typescript.compiler.TypeScriptCompilerConfigUtil
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptFileObject
import com.intellij.openapi.vfs.VirtualFile

class Angular2TranspiledTemplateRequestArgs private constructor(
  file: VirtualFile,
  @JvmField val content: String?,
  @JvmField val transpiledContent: String?,
  @JvmField val mappings: List<CodeMapping>,
) : TypeScriptFileObject() {

  init {
    this.file = LocalFilePath.create(TypeScriptCompilerConfigUtil.normalizeNameAndPath(file))
  }

  companion object {
    fun build(
      file: VirtualFile,
      content: CharSequence?,
      transpiledContent: CharSequence?,
      mappings: List<CodeMapping>
    ): Angular2TranspiledTemplateRequestArgs {
      return Angular2TranspiledTemplateRequestArgs(file, content?.toString(), transpiledContent?.toString(), mappings)
    }
  }
}

class CodeMapping(
  @JvmField val source: String, /* source file name */
  @JvmField val sourceOffsets: List<Int>,
  @JvmField val generatedOffsets: List<Int>,
  @JvmField val lengths: List<Int>,
  @JvmField val generatedLengths: List<Int>,
)
