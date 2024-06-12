package org.angular2.lang.expr.service.protocol.commands

import com.intellij.lang.javascript.service.protocol.LocalFilePath
import com.intellij.lang.typescript.compiler.TypeScriptCompilerConfigUtil
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptFileObject
import com.intellij.openapi.vfs.VirtualFile
import org.angular2.lang.html.tcb.Angular2TranspiledComponentFileBuilder
import org.angular2.lang.html.tcb.Angular2TranspiledComponentFileBuilder.TranspiledComponentFile

class Angular2TranspiledTemplateRequestArgs private constructor(
  file: VirtualFile,
  @JvmField val transpiledContent: String?,
  @JvmField val sourceCode: Map<String, String>,
  @JvmField val mappings: List<CodeMapping>,
) : TypeScriptFileObject() {

  init {
    this.file = LocalFilePath.create(TypeScriptCompilerConfigUtil.normalizeNameAndPath(file))
  }

  companion object {
    fun build(
      file: VirtualFile,
      transpiledContent: String,
      sourceCode: Map<String, String>,
      mappings: List<CodeMapping>,
    ): Angular2TranspiledTemplateRequestArgs {
      return Angular2TranspiledTemplateRequestArgs(file, transpiledContent, sourceCode, mappings)
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

internal fun TranspiledComponentFile.toAngular2TranspiledTemplateRequestArgs(virtualFile: VirtualFile): Angular2TranspiledTemplateRequestArgs =
  Angular2TranspiledTemplateRequestArgs.build(
    virtualFile,
    this.generatedCode,
    this.mappings.associate {
      Pair(it.fileName, it.sourceFile.text)
    },
    this.mappings.map {
      it.toCodeMapping()
    }
  )

private fun Angular2TranspiledComponentFileBuilder.FileMappings.toCodeMapping(): CodeMapping {
  val fileName = fileName
  val mappings = sourceMappings
  val prevMappingStart = -1
  val prevMappingLength = 0
  mappings.forEach {
    if (
      it.sourceOffset == prevMappingStart
      && it.sourceLength != prevMappingLength
    ) {
      throw IllegalStateException("Overlapping source mappings in $fileName: ($prevMappingStart:$prevMappingLength) and (${it.sourceOffset}:${it.sourceLength})")
    }
  }
  return CodeMapping(
    source = fileName,
    sourceOffsets = mappings.map { it.sourceOffset },
    generatedOffsets = mappings.map { it.generatedOffset },
    lengths = mappings.map { it.sourceLength },
    generatedLengths = mappings.map { it.generatedLength }
  )
}


