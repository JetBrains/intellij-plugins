// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.service

import com.google.gson.JsonObject
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.integration.JSAnnotationRangeError
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.JSEvaluationStatisticsCollector
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceProtocol
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.javascript.service.withScopedTraceSpan
import com.intellij.lang.javascript.service.withTraceSpan
import com.intellij.lang.typescript.compiler.TypeScriptCompilerServiceRequest
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceAnnotationResult
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceWidgetItem
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCache
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.InlayHintItem
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.InlayHintKind
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptInlayHintsResponse
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.openapi.application.ReadAction.computeCancellable
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.progress.runBlockingMaybeCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
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
import org.angular2.lang.html.tcb.Angular2TranspiledComponentFileBuilder
import org.angular2.lang.html.tcb.Angular2TranspiledComponentFileBuilder.TranspiledComponentFile
import org.angular2.lang.html.tcb.Angular2TranspiledComponentFileBuilder.getTranspiledComponentAndTopLevelTemplateFile
import org.angular2.options.AngularConfigurable
import org.angular2.options.AngularServiceSettings
import org.angular2.options.AngularSettings
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
    runBlockingMaybeCancellable {
      smartReadAction(file.project) {
        val (transpiledComponentFile, templateFile) = getTranspiledComponentAndTopLevelTemplateFile(file)
                                                      ?: return@smartReadAction list
        translateNamesInErrors(list, transpiledComponentFile, templateFile)
      }
    }

  override fun supportsInlayHints(file: PsiFile): Boolean =
    file.language is Angular2HtmlDialect || super.supportsInlayHints(file)

  override suspend fun getInlayHints(file: PsiFile, textRange: TextRange): TypeScriptInlayHintsResponse? = withScopedTraceSpan(javaClass, "getInlayHintsAngular", myLifecycleSpan) {
    val hasTranspiledTemplate = refreshTranspiledTemplateIfNeededCancellable(file.virtualFile ?: return@withScopedTraceSpan null) != null
    val result = super.getInlayHints(file, textRange) ?: return@withScopedTraceSpan null
    return@withScopedTraceSpan readAction {
      if (hasTranspiledTemplate)
        repositionInlayHints(file, result)
      else
        result
    }
  }

  private fun repositionInlayHints(file: PsiFile, hints: Array<InlayHintItem>): Array<InlayHintItem> = withTraceSpan(javaClass, "repositionInlayHints") {
    val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return@withTraceSpan hints
    return@withTraceSpan hints.map {
      repositionInlayHint(file, document, it)
    }.toTypedArray()
  }

  override fun isTypeEvaluationEnabled(): Boolean = project.service<AngularSettings>().serviceType != AngularServiceSettings.DISABLED

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
    refreshTranspiledTemplateIfNeeded(file)
  }

  override fun isGeterrSupported(psiFile: PsiFile): Boolean {
    return psiFile.language.let { it !is Angular2Language && it !is Angular2HtmlDialect }
  }

  private fun translateNamesInErrors(errors: List<JSAnnotationError>, file: TranspiledComponentFile, templateFile: PsiFile): List<JSAnnotationError> = withTraceSpan(javaClass, "translateNamesInErrors") {
    val document = PsiDocumentManager.getInstance(templateFile.project).getDocument(templateFile)
                   ?: return@withTraceSpan emptyList()
    return@withTraceSpan errors.map { error ->
      if (error is TypeScriptLanguageServiceAnnotationResult) {
        val textRange = error.getTextRange(document)
                        ?: return@map error
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

  private fun repositionInlayHint(file: PsiFile, document: Document, hint: InlayHintItem): InlayHintItem {
    if (hint.kind != InlayHintKind.Type) return hint
    val line = hint.position?.line ?: return hint
    val column = hint.position?.offset ?: return hint
    val offset = document.getLineStartOffset(line - 1) + column - 1
    val injectedLanguageManager = InjectedLanguageManager.getInstance(file.project)
    val injectedElement = injectedLanguageManager.findInjectedElementAt(file, offset)
    val textRange = if (injectedElement != null)
      injectedElement.takeIf(::acceptElementToRepositionHint)?.textRange?.let {
        injectedLanguageManager.injectedToHost(injectedElement, it)
      }
    else
      file.findElementAt(offset)?.takeIf(::acceptElementToRepositionHint)?.textRange
    if (textRange == null || textRange.endOffset == offset || textRange.startOffset == offset) return hint
    hint.position!!.offset += textRange.endOffset - offset
    return hint
  }

  private fun acceptElementToRepositionHint(element: PsiElement): Boolean =
    element is LeafPsiElement
    && element.containingFile.language.let { it is Angular2HtmlDialect || it is Angular2Language }

  private fun refreshTranspiledTemplateIfNeeded(virtualFile: VirtualFile): TranspiledComponentFile? = withTraceSpan(javaClass, "refreshTranspiledTemplateIfNeededCancellable") {
    JSLanguageServiceUtil.nonBlockingReadActionWithTimeout {
      // Updating the cache can cause the transpiled template to be (re)built,
      // so let's build the template first and ensure that it doesn't change
      // by keeping the read action lock. Otherwise, we can get unnecessary cancellations
      // on server cache locking leading to tests instability.
      val result = Angular2TranspiledComponentFileBuilder.getTranspiledComponentFileForTemplateFile(myProject, virtualFile)
      runBlockingCancellable {
        process?.executeCancellable(Angular2TranspiledTemplateCommand(virtualFile))
      }
      result
    }
  }

  private suspend fun refreshTranspiledTemplateIfNeededCancellable(virtualFile: VirtualFile): TranspiledComponentFile? = withScopedTraceSpan(javaClass, "refreshTranspiledTemplateIfNeededCancellable") {
    readAction {
      // Updating the cache can cause the transpiled template to be (re)built,
      // so let's build the template first and ensure that it doesn't change
      // by keeping the read action lock. Otherwise, we can get unnecessary cancellations
      // on server cache locking leading to tests instability.
      val result = Angular2TranspiledComponentFileBuilder.getTranspiledComponentFileForTemplateFile(myProject, virtualFile)
      runBlockingCancellable {
        process?.executeCancellable(Angular2TranspiledTemplateCommand(virtualFile))
      }
      result
    }
  }

  private inner class Angular2CompilerServiceEvaluationSupport(project: Project) : TypeScriptCompilerServiceEvaluationSupport(project),
                                                                                   Angular2TypeScriptServiceEvaluationSupport {

    override val service: TypeScriptService
      get() = this@Angular2TypeScriptService

    override fun getElementType(element: PsiElement, virtualFile: VirtualFile, projectFileName: String?): JSType? =
      if (element !is JSElement && element.parent !is JSElement) null else super.getElementType(element, virtualFile, projectFileName)

    override fun commitDocumentsBeforeGetElementType(element: PsiElement, virtualFile: VirtualFile) {
      commitDocumentsWithNBRA(virtualFile)
      if (element.language is Angular2Language || element.language is Angular2HtmlDialect) {
        refreshTranspiledTemplateIfNeeded(virtualFile)
      }
    }

    override fun getGeneratedElementType(transpiledFile: TranspiledComponentFile, templateFile: PsiFile, generatedRange: TextRange): JSType? = withTraceSpan(javaClass, "getGeneratedElementType", myLifecycleSpan) {
      val componentVirtualFile = transpiledFile.originalFile.originalFile.virtualFile
                                 ?: return@withTraceSpan null
      val evaluationLocation = InjectedLanguageManager.getInstance(templateFile.project).getTopLevelFile(templateFile.originalFile).virtualFile
                               ?: return@withTraceSpan null
      commitDocumentsWithNBRA(componentVirtualFile)
      if (componentVirtualFile != evaluationLocation) {
        // If template is not inlined, we need to ensure that both component and template files are up-to-date
        commitDocumentsWithNBRA(evaluationLocation)
      }
      // Ensure that transpiled template is up-to-date
      refreshTranspiledTemplateIfNeeded(componentVirtualFile)

      val filePath = getFilePath(componentVirtualFile) ?: return@withTraceSpan null

      // The evaluation location is in the template, so the config will be searched for the containing component file,
      // which is the transpiledFile.originalFile
      val projectFileName = TypeScriptConfigUtil.getProjectFileName(transpiledFile.originalFile.originalFile)
      val args = Angular2GetGeneratedElementTypeRequestArgs(filePath, projectFileName, generatedRange.startOffset, generatedRange.endOffset)
      return@withTraceSpan sendGetElementTypeCommandAndDeserializeResponse(null, args, ::getGeneratedElementType)
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
) : TypeScriptCompilerServiceRequest<Angular2GetGeneratedElementTypeRequestArgs, JsonObject>(args, service, coroutineContext) {
  override fun createCommand(): JSLanguageServiceSimpleCommand = Angular2GetGeneratedElementTypeCommand(args)
  override suspend fun processResult(answer: JsonObject): JsonObject? = answer.getAsJsonObject(TypeScriptServerServiceImpl.BODY_FIELD)
}

private fun isAngularServiceSupport(project: Project, context: VirtualFile): Boolean =
  WebSymbolsContext.get("angular-service-support", context, project) != "false"