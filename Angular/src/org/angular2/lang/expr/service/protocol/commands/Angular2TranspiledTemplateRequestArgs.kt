package org.angular2.lang.expr.service.protocol.commands

import com.intellij.lang.javascript.service.protocol.LocalFilePath
import com.intellij.lang.typescript.compiler.TypeScriptCompilerConfigUtil
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptFileObject
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile
import java.util.*

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

@Suppress("unused")
class Angular2TcbMappingInfo(
  @JvmField val fileName: String, /* source file name */
  @JvmField val sourceOffsets: List<Int>,
  @JvmField val sourceLengths: List<Int>,
  @JvmField val generatedOffsets: List<Int>,
  @JvmField val generatedLengths: List<Int>,
  @JvmField val diagnosticsOffsets: List<Int>,
  @JvmField val diagnosticsLengths: List<Int>,
  @JvmField val flags: List<Int>,
)

internal fun TranspiledDirectiveFile.toAngular2TranspiledTemplateRequestArgs(project: Project, virtualFile: VirtualFile): Angular2TranspiledTemplateRequestArgs {

  val psiDocumentManager = PsiDocumentManager.getInstance(project)
  val fileDocumentManager = FileDocumentManager.getInstance()

  val fileContentsAndSourceMappingOffsetsMap = this.fileMappings.values.associateBy({ it.fileName }, {
    val sourceFile = it.sourceFile
    val document = psiDocumentManager.getDocument(sourceFile)
    val sourceVirtualFile = sourceFile.originalFile.virtualFile
    val sourceFileLineSeparator = fileDocumentManager.getLineSeparator(sourceVirtualFile, sourceFile.project)
    val sourceFileText = document?.text ?: sourceFile.text

    // This remapping is needed to accomodate for the added CRLF to files sent to the TS server.
    // We need to do it for the generated file and for source files, as all files sent to TS server are converted
    // to original line endings.
    Pair(
      sourceFileText,
      if (sourceFileLineSeparator.length == 1)
        null
      else
        buildCodeMappingOffsets(StringUtilRt.convertLineSeparators(sourceFileText, sourceFileLineSeparator))
    )
  })

  val componentFileLineSeparator = fileDocumentManager.getLineSeparator(virtualFile, project)
  val (generatedCode, generatedMappingsOffsets) = if (componentFileLineSeparator.length > 1)
  // The generated code needs to have line endings similar to what we send to TS server without transpilation
    StringUtilRt.convertLineSeparators(this.generatedCode, componentFileLineSeparator).let {
      Pair(it, buildCodeMappingOffsets(it))
    }
  else
    Pair(this.generatedCode, null)

  return Angular2TranspiledTemplateRequestArgs.build(
    virtualFile,
    generatedCode,
    this.fileMappings.values.associate {
      Pair(it.fileName, fileContentsAndSourceMappingOffsetsMap[it.fileName]!!.first)
    },
    this.fileMappings.values.map {
      it.toCodeMapping(fileContentsAndSourceMappingOffsetsMap[it.fileName]?.second, generatedMappingsOffsets)
    }
  )
}

private fun Angular2TranspiledDirectiveFileBuilder.FileMappings.toCodeMapping(
  sourceMappingOffsets: NavigableMap<Int, Int>?,
  generatedMappingOffsets: NavigableMap<Int, Int>?,
): Angular2TcbMappingInfo {
  val mappings = sourceMappings.filter { !it.ignored }
  return Angular2TcbMappingInfo(
    fileName = fileName,
    sourceOffsets = mappings.map { it.sourceOffset.translate(sourceMappingOffsets) },
    sourceLengths = mappings.map { it.sourceLength.translateLength(it.sourceOffset, sourceMappingOffsets) },
    generatedOffsets = mappings.map { it.generatedOffset.translate(generatedMappingOffsets) },
    generatedLengths = mappings.map { it.generatedLength.translateLength(it.generatedOffset, generatedMappingOffsets) },
    diagnosticsOffsets = mappings.map { it.diagnosticsOffset?.translate(sourceMappingOffsets) ?: -1 },
    diagnosticsLengths = mappings.map { it.diagnosticsLength?.translateLength(it.diagnosticsOffset ?: 0, sourceMappingOffsets) ?: -1 },
    flags = mappings.map { it.flags.sumOf { flag -> 1 shl flag.ordinal } },
  )
}

private fun buildCodeMappingOffsets(
  text: @NlsSafe CharSequence,
): NavigableMap<Int, Int> {
  val result = TreeMap<Int, Int>()
  result[0] = 0
  var curOffset = 0
  for (match in Regex("(\n\r|\r\n|\n)").findAll(text, 0)) {
    val matchLength = match.value.length
    if (matchLength > 1) {
      curOffset += matchLength - 1
      val matchStart = match.range.first
      result[matchStart + matchLength - curOffset] = curOffset
    }
  }
  return result
}

private fun Int.translate(sourceMappingOffsets: NavigableMap<Int, Int>?): Int =
  if (sourceMappingOffsets == null)
    this
  else
    this + sourceMappingOffsets.floorEntry(this).value

private fun Int.translateLength(startOffset: Int, sourceMappingOffsets: NavigableMap<Int, Int>?): Int =
  if (sourceMappingOffsets == null)
    this
  else
    (this + startOffset).translate(sourceMappingOffsets) - startOffset.translate(sourceMappingOffsets)