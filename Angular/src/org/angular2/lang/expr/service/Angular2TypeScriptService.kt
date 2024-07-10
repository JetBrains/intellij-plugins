// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.service

import com.google.gson.JsonObject
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.integration.JSAnnotationRangeError
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.JSEvaluationStatisticsCollector
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceProtocol
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.typescript.compiler.TypeScriptCompilerServiceRequest
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceAnnotationResult
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceWidgetItem
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCache
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.openapi.application.ReadAction.computeCancellable
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.application
import com.intellij.util.asSafely
import com.intellij.util.indexing.SubstitutedFileType
import com.intellij.util.ui.EDT
import com.intellij.webSymbols.context.WebSymbolsContext
import icons.AngularIcons
import kotlinx.coroutines.currentCoroutineContext
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.Angular2LangUtil.isAngular2Context
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.service.protocol.Angular2TypeScriptServiceProtocol
import org.angular2.lang.expr.service.protocol.commands.Angular2GetGeneratedElementTypeCommand
import org.angular2.lang.expr.service.protocol.commands.Angular2GetGeneratedElementTypeRequestArgs
import org.angular2.lang.expr.service.protocol.commands.Angular2TranspiledTemplateCommand
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.lang.html.tcb.Angular2TranspiledComponentFileBuilder.TranspiledComponentFile
import org.angular2.lang.html.tcb.Angular2TranspiledComponentFileBuilder.getTranspiledComponentAndTopLevelTemplateFile
import org.angular2.options.AngularConfigurable
import org.angular2.options.AngularServiceSettings
import org.angular2.options.getAngularSettings
import org.intellij.images.fileTypes.impl.SvgFileType
import java.util.concurrent.Future
import java.util.function.Consumer
import kotlin.coroutines.CoroutineContext

class Angular2TypeScriptService(project: Project) : TypeScriptServerServiceImpl(project, "Angular Console") {

  override fun getProcessName(): String = "Angular TypeScript"

  override fun isAcceptable(file: VirtualFile): Boolean =
    super.isAcceptable(file) || isAcceptableHtmlFile(file)

  override fun isAcceptableNonTsFile(project: Project, service: TypeScriptConfigService, virtualFile: VirtualFile): Boolean {
    return super.isAcceptableNonTsFile(project, service, virtualFile) || isAcceptableHtmlFile(virtualFile)
  }

  private fun isAcceptableHtmlFile(file: VirtualFile): Boolean =
    file.isInLocalFileSystem && file.fileType.let {
      (it is HtmlFileType || it is SvgFileType) && SubstitutedFileType.substituteFileType(file, it, project).asSafely<SubstitutedFileType>()?.language is Angular2HtmlDialect
    }

  override fun supportsInjectedFile(file: PsiFile): Boolean {
    return file.language is Angular2Language || file.language is Angular2HtmlDialect
  }

  override fun isAcceptableForHighlighting(file: PsiFile): Boolean =
    if (file.language is Angular2HtmlDialect || file.language is Angular2Language)
      Angular2EntitiesProvider.findTemplateComponent(file) != null
    else
      super.isAcceptableForHighlighting(file)

  override fun getQuickInfoAt(usageElement: PsiElement, originalFile: VirtualFile): Future<TypeScriptQuickInfoResponse?>? =
    if (usageElement.containingFile.language.let { it is Angular2HtmlDialect || it is Angular2Language })
      null // For now do not use TS server for quick info
    else
      super.getQuickInfoAt(usageElement, originalFile)

  override fun postprocessErrors(file: PsiFile, list: List<JSAnnotationError>): List<JSAnnotationError> =
    computeCancellable<List<JSAnnotationError>, Throwable> {
      val (transpiledComponentFile, templateFile) = getTranspiledComponentAndTopLevelTemplateFile(file)
                                                    ?: return@computeCancellable list

      translateNamesInErrors(list, transpiledComponentFile, templateFile)
    }

  override val typeEvaluationSupport: Angular2TypeScriptServiceEvaluationSupport = Angular2CompilerServiceEvaluationSupport(project)

  override fun supportsTypeEvaluation(virtualFile: VirtualFile, element: PsiElement): Boolean =
    (element.language.let { it is Angular2Language || it is Angular2HtmlDialect }
     && Angular2EntitiesProvider.findTemplateComponent(element) != null
    ) || super.supportsTypeEvaluation(virtualFile, element)

  override fun isDisabledByContext(context: VirtualFile): Boolean =
    super.isDisabledByContext(context)
    || !isAngularServiceAvailableByContext(context)

  private fun isAngularServiceAvailableByContext(context: VirtualFile): Boolean =
    isAngularTypeScriptServiceEnabled(myProject, context)

  override fun createProtocol(readyConsumer: Consumer<*>, tsServicePath: String): JSLanguageServiceProtocol =
    Angular2TypeScriptServiceProtocol(myProject, mySettings, readyConsumer, createEventConsumer(), tsServicePath)

  override fun createWidgetItem(currentFile: VirtualFile?): LanguageServiceWidgetItem =
    TypeScriptServiceWidgetItem(this, currentFile, AngularIcons.Angular2, AngularIcons.Angular2, AngularConfigurable::class.java)

  override fun createLSCache(): TypeScriptLanguageServiceCache =
    Angular2LanguageServiceCache(myProject)

  override fun beforeGetErrors(file: VirtualFile) {
    process?.executeNoBlocking(Angular2TranspiledTemplateCommand(file), null, null)
  }

  private fun translateNamesInErrors(errors: List<JSAnnotationError>, file: TranspiledComponentFile, templateFile: PsiFile): List<JSAnnotationError> {
    val document = PsiDocumentManager.getInstance(templateFile.project).getDocument(templateFile)
                   ?: return emptyList()
    return errors.map { error ->
      if (error is TypeScriptLanguageServiceAnnotationResult) {
        val textRange = error.getTextRange(document)
        val nameMap = file.nameMaps[templateFile]
          ?.subMap(textRange.startOffset, true, textRange.endOffset, false)
          ?.values
          ?.asSequence()
          ?.flatMap { it.entries }
          ?.map { (key, value) -> key to value }
          ?.toMap()
        if (nameMap != null) {
          return@map TypeScriptLanguageServiceAnnotationResult(
            error.description.replaceNames("'", nameMap, "'"),
            error.absoluteFilePath,
            error.category,
            error.source,
            error.tooltipText?.replaceNames(">", nameMap, "<"),
            error.errorCode, error.line + 1, error.column + 1, error.endLine + 1, error.endColumn + 1,
          )
        }
      }
      return@map error
    }
  }

  private inner class Angular2CompilerServiceEvaluationSupport(project: Project) : TypeScriptCompilerServiceEvaluationSupport(project),
                                                                                   Angular2TypeScriptServiceEvaluationSupport {

    override val service: TypeScriptService
      get() = this@Angular2TypeScriptService

    override fun getElementType(element: PsiElement, virtualFile: VirtualFile, evaluationLocation: VirtualFile): JSType? =
      if (element !is JSElement && element.parent !is JSElement) null else super.getElementType(element, virtualFile, evaluationLocation)

    override fun commitDocumentsBeforeGetElementType(element: PsiElement, virtualFile: VirtualFile) {
      commitDocumentsWithNBRA(virtualFile)
      if (element.language is Angular2Language || element.language is Angular2HtmlDialect) {
        process?.executeNoBlocking(Angular2TranspiledTemplateCommand(virtualFile), null, null)
      }
    }

    override fun getGeneratedElementType(transpiledFile: TranspiledComponentFile, templateFile: PsiFile, generatedRange: TextRange): JSType? {
      val virtualFile = transpiledFile.originalFile.originalFile.virtualFile
                        ?: return null
      val evaluationLocation = templateFile.originalFile.virtualFile
                               ?: return null
      commitDocumentsWithNBRA(virtualFile)
      if (virtualFile != evaluationLocation) {
        commitDocumentsWithNBRA(evaluationLocation)
      }
      process?.executeNoBlocking(Angular2TranspiledTemplateCommand(virtualFile), null, null)

      val filePath = getFilePath(virtualFile) ?: return null

      val args = Angular2GetGeneratedElementTypeRequestArgs(filePath, generatedRange.startOffset, generatedRange.endOffset)
      return sendGetElementTypeCommandAndDeserializeResponse(null, args, ::getGeneratedElementType)
    }

    suspend fun getGeneratedElementType(args: Angular2GetGeneratedElementTypeRequestArgs): JsonObject? {
      val task = Angular2GetGeneratedElementTypeRequest(args, this@Angular2TypeScriptService, currentCoroutineContext())
      val response = requestQueue.request(task)
      if (JSEvaluationStatisticsCollector.State.isEnabled()) {
        application.service<JSEvaluationStatisticsCollector>().responseReady(!task.wasExecuted)
      }
      return response
    }
  }
}

private fun JSAnnotationRangeError.getTextRange(document: Document): TextRange {
  val startOffset = document.getLineStartOffset(this.line) + this.column
  val endOffset = document.getLineStartOffset(this.endLine) + this.endColumn
  return TextRange(startOffset, endOffset)
}

private fun String.replaceNames(prefix: String, nameMap: Map<String, String>, suffix: String): String {
  var result = this
  for ((generatedName, originalName) in nameMap) {
    result = result.replace(Regex("([$prefix.])${Regex.escape(generatedName)}([$suffix.])"), "\$1$originalName\$2")
  }
  return result
}

fun isAngularTypeScriptServiceEnabled(project: Project, context: VirtualFile): Boolean {
  val isAngularServiceContext = if (EDT.isCurrentThreadEdt())
    isAngular2Context(project, context) && isAngularServiceSupport(project, context)
  else
    computeCancellable<Boolean, Throwable> {
      isAngular2Context(project, context) && isAngularServiceSupport(project, context)
    }

  if (!isAngularServiceContext) return false

  return when (getAngularSettings(project).serviceType) {
    AngularServiceSettings.AUTO -> true
    AngularServiceSettings.DISABLED -> false
  }
}

private class Angular2GetGeneratedElementTypeRequest(
  args: Angular2GetGeneratedElementTypeRequestArgs,
  service: Angular2TypeScriptService,
  coroutineContext: CoroutineContext,
) : TypeScriptCompilerServiceRequest<Angular2GetGeneratedElementTypeRequestArgs>(args, service, coroutineContext) {
  override fun createCommand(): JSLanguageServiceSimpleCommand = Angular2GetGeneratedElementTypeCommand(args)
}

private fun isAngularServiceSupport(project: Project, context: VirtualFile): Boolean =
  WebSymbolsContext.get("angular-service-support", context, project) != "false"