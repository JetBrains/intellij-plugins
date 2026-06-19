@file:JvmName("Angular2ServiceUtils")

package org.angular2.lang.expr.service

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.integration.JSAnnotationRangeError
import com.intellij.lang.typescript.compiler.TypeScriptServiceHolder
import com.intellij.lang.typescript.compiler.languageService.TypeScriptAnnotationRangeError
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.InlayHintItem
import com.intellij.lang.typescript.kolar.TypeScriptInlayHint
import com.intellij.lang.typescript.kolar.TypeScriptInlayHint.InlayHintKind
import com.intellij.lang.typescript.lsp.TypeScriptGoLspService
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.polySymbols.context.PolyContext
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ui.EDT
import org.angular2.lang.Angular2LangUtil.isAngular2Context
import org.angular2.lang.expr.Angular2ExprDialect
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.options.AngularServiceSettings
import org.angular2.options.getAngularSettings
import java.nio.file.Path
import java.util.concurrent.Callable
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

typealias ResponseInlayHintKind = com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.InlayHintKind

fun isAngularTypeScriptServiceEnabled(project: Project, context: VirtualFile): Boolean {
  val evaluationLocation = JSTypeEvaluationLocationProvider.typeEvaluationLocation

  val isAngularServiceContext = if (EDT.isCurrentThreadEdt())
    (isAngular2Context(project, context) || (evaluationLocation != null && isAngular2Context(evaluationLocation)))
    && isAngularServiceSupport(project, context)
  else
    ReadAction.nonBlocking(Callable {
      (isAngular2Context(project, context) || (evaluationLocation != null && isAngular2Context(evaluationLocation)))
      && isAngularServiceSupport(project, context)
    }).executeSynchronously()

  if (!isAngularServiceContext) return false

  return when (getAngularSettings(project).serviceType) {
    AngularServiceSettings.AUTO -> true
    AngularServiceSettings.DISABLED -> false
  }
}

fun isAngularTypeScriptServiceEnabled(context: PsiFile): Boolean =
  TypeScriptServiceHolder.getForElement(context)?.service.let {
    it is Angular2TypeScriptService
    || (it is TypeScriptGoLspService && isAngularTypeScriptServiceEnabled(context.project, context.virtualFile))
  }

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

fun repositionInlayHint(file: PsiFile, document: Document, hint: TypeScriptInlayHint): TypeScriptInlayHint? {
  if (hint.kind.let { it != InlayHintKind.Type && it != InlayHintKind.Parameter }) return hint
  val offset = document.getLineStartOffset(hint.position.line) + hint.position.column
  val injectedLanguageManager = InjectedLanguageManager.getInstance(file.project)
  val injectedElement = injectedLanguageManager.findInjectedElementAt(file, offset)
  when (hint.kind) {
    InlayHintKind.Type -> {
      val textRange = if (injectedElement != null)
        injectedElement.takeIf(::acceptElementToRepositionHint)?.textRange?.let {
          injectedLanguageManager.injectedToHost(injectedElement, it)
        }
      else
        file.findElementAt(offset)?.takeIf(::acceptElementToRepositionHint)?.textRange
      if (textRange == null || textRange.endOffset == offset || textRange.startOffset == offset) return hint
      // Reposition hint
      hint.position = hint.position.copy(column = hint.position.column + textRange.endOffset - offset)
      return hint
    }
    InlayHintKind.Parameter -> {
      val element = injectedElement ?: file.findElementAt(offset)
      return hint.takeIf { element !is XmlAttribute && element?.parent !is XmlAttribute }
    }
    else -> return hint
  }
}

fun wrapInlayHintItem(item: InlayHintItem, transformer: (TypeScriptInlayHint) -> TypeScriptInlayHint?): InlayHintItem? {
  val itemKind = item.kind ?: return item
  val itemPosition = item.position ?: return item

  val wrapper = object : TypeScriptInlayHint {
    override var position: TypeScriptInlayHint.InlayHintLocation
      get() = TypeScriptInlayHint.InlayHintLocation(itemPosition.line - 1, itemPosition.offset - 1)
      set(value) {
        itemPosition.let {
          it.line = value.line + 1
          it.offset = value.column + 1
        }
      }
    override val kind: InlayHintKind
      get() = when (itemKind) {
        ResponseInlayHintKind.Type -> InlayHintKind.Type
        ResponseInlayHintKind.Parameter -> InlayHintKind.Parameter
        ResponseInlayHintKind.Enum -> InlayHintKind.Enum
      }
  }
  if (transformer(wrapper) == null) {
    return null
  }
  return item
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

private fun acceptElementToRepositionHint(element: PsiElement): Boolean =
  element is LeafPsiElement
  && element.containingFile.language.let { it is Angular2HtmlDialect || it is Angular2ExprDialect }

private fun isAngularServiceSupport(project: Project, context: VirtualFile): Boolean =
  PolyContext.get("angular-service-support", context, project) != "false"
