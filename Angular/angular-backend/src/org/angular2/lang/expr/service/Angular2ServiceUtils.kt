@file:JvmName("Angular2ServiceUtils")

package org.angular2.lang.expr.service

import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.integration.JSAnnotationRangeError
import com.intellij.lang.typescript.compiler.languageService.TypeScriptAnnotationRangeError
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

fun <Items : JSAnnotationError, T : TypeScriptAnnotationRangeError> translateNamesInErrors(
  errors: List<Items>,
  file: TranspiledDirectiveFile,
  templateFile: PsiFile,
  errorClass: KClass<T>,
  resultCopyFactory: (error: T, newDescription: String, newTooltip: String?) -> T,
): List<Items> {
  val document = PsiDocumentManager.getInstance(templateFile.project).getDocument(templateFile)
                 ?: return emptyList()
  val absoluteFilePath = templateFile.virtualFile?.toNioPathOrNull() ?: return errors
  val checkCache = mutableMapOf<String, Boolean>()
  return errors.map { jsAnnotationError ->
    val error = errorClass.safeCast(jsAnnotationError)
                ?: return@map jsAnnotationError
    if (error.line < 0 || !checkCache.computeIfAbsent(error.absoluteFilePath ?: "") { absoluteFilePath == Path.of(it) })
      return@map jsAnnotationError
    val textRange =
      try {
        error.getTextRange(document)
      }
      catch (e: Exception) {
        logger<Angular2TypeScriptService>().error(e)
        null
      } ?: return@map jsAnnotationError
    val nameMap = file.nameMaps[templateFile]
      ?.subMap(textRange.startOffset, true, textRange.endOffset, false)
      ?.values
      ?.asSequence()
      ?.flatMap { it.entries }
      ?.associate { (key, value) -> key to value }
    if (nameMap != null) {
      @Suppress("UNCHECKED_CAST")
      return@map resultCopyFactory(
        error,
        error.description.replaceNames("'", nameMap, "'"),
        error.tooltipText?.replaceNames(">", nameMap, "<")
      ) as Items
    }
    return@map jsAnnotationError
  }
}

private fun JSAnnotationRangeError.getTextRange(document: Document): TextRange? {
  val startOffset = document.getLineStartOffset(this.line) + this.column
  val endOffset = document.getLineStartOffset(this.endLine) + this.endColumn
  return if (startOffset in 0..endOffset) TextRange(startOffset, endOffset) else null
}

private fun String.replaceNames(prefix: String, nameMap: Map<String, String>, suffix: String): String {
  var result = this
  for ((generatedName, originalName) in nameMap) {
    result = result.replace(Regex("$prefix${Regex.escape(generatedName)}([.$suffix])"), "$prefix$originalName\$1")
  }
  return result
}
