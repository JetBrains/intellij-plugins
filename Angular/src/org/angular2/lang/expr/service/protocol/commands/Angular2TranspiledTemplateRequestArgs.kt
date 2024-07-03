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
  @JvmField val mappings: List<Angular2TcbMappingInfo>,
) : TypeScriptFileObject() {

  init {
    this.file = LocalFilePath.create(TypeScriptCompilerConfigUtil.normalizeNameAndPath(file))
  }

  companion object {
    fun build(
      file: VirtualFile,
      transpiledContent: String,
      sourceCode: Map<String, String>,
      mappings: List<Angular2TcbMappingInfo>,
    ): Angular2TranspiledTemplateRequestArgs {
      return Angular2TranspiledTemplateRequestArgs(file, transpiledContent, sourceCode, mappings)
    }
  }
}

class Angular2TcbMappingInfo(
  @JvmField val fileName: String, /* source file name */
  @JvmField val sourceOffsets: List<Int>,
  @JvmField val sourceLengths: List<Int>,
  @JvmField val generatedOffsets: List<Int>,
  @JvmField val generatedLengths: List<Int>,
  @JvmField val diagnosticsOffsets: List<Int>,
  @JvmField val diagnosticsLengths: List<Int>,
  @JvmField val types: List<Int>,
)

internal fun TranspiledComponentFile.toAngular2TranspiledTemplateRequestArgs(virtualFile: VirtualFile): Angular2TranspiledTemplateRequestArgs =
  Angular2TranspiledTemplateRequestArgs.build(
    virtualFile,
    this.generatedCode,
    this.fileMappings.values.associate {
      Pair(it.fileName, it.sourceFile.text)
    },
    this.fileMappings.values.map {
      it.toCodeMapping()
    }
  )

private fun Angular2TranspiledComponentFileBuilder.FileMappings.toCodeMapping(): Angular2TcbMappingInfo {
  val mappings = sourceMappings.filter { !it.ignored }
  return Angular2TcbMappingInfo(
    fileName = fileName,
    sourceOffsets = mappings.map { it.sourceOffset },
    sourceLengths = mappings.map { it.sourceLength },
    generatedOffsets = mappings.map { it.generatedOffset },
    generatedLengths = mappings.map { it.generatedLength },
    diagnosticsOffsets = mappings.map { it.diagnosticsOffset ?: -1 },
    diagnosticsLengths = mappings.map { it.diagnosticsLength ?: -1 },
    types = mappings.map { if (it.types) 1 else 0 }
  )
}


