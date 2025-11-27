// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.javascript.typeEngine.JSServicePoweredTypeEngineUsageContext
import com.intellij.javascript.types.TSType
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.integration.JSAnnotationRangeError
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.javascript.service.readActionWithSuspend
import com.intellij.lang.javascript.service.withScopedServiceTraceSpan
import com.intellij.lang.javascript.service.withServiceTraceSpan
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.TypeScriptServiceEvaluationSupport.UpdateContextInfo
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceAnnotationResult
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceQueueImpl
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceWidgetItem
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCache
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptServiceStandardOutputProtocol
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.Position
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.Range
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptTypeRequestKind
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.InlayHintItem
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.InlayHintKind
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptInlayHintsResult
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.openapi.application.ReadAction.computeCancellable
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.polySymbols.context.PolyContext
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.asSafely
import com.intellij.util.concurrency.ThreadingAssertions
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.indexing.SubstitutedFileType
import com.intellij.util.ui.EDT
import icons.AngularIcons
import org.angular2.Angular2DecoratorUtil.isHostBindingExpression
import org.angular2.codeInsight.blocks.isDeferOnReferenceExpression
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.Angular2LangUtil.isAngular2Context
import org.angular2.lang.expr.Angular2ExprDialect
import org.angular2.lang.expr.service.protocol.Angular2TypeScriptServiceProtocol
import org.angular2.lang.expr.service.protocol.commands.Angular2GetGeneratedElementTypeCommand
import org.angular2.lang.expr.service.protocol.commands.Angular2GetGeneratedElementTypeRequestArgs
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder.getTranspiledDirectiveAndTopLevelSourceFile
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.options.AngularConfigurable
import org.angular2.options.AngularServiceSettings
import org.angular2.options.AngularSettings
import org.angular2.options.getAngularSettings
import org.intellij.images.fileTypes.impl.SvgFileType
import java.util.concurrent.Future

class Angular2TypeScriptService(project: Project) : TypeScriptServerServiceImpl(project, "AngularService") {

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
    return file.language is Angular2ExprDialect || file.language is Angular2HtmlDialect
  }

  override fun isAcceptableForHighlighting(file: PsiFile): Boolean =
    if (file.language is Angular2HtmlDialect || file.language is Angular2ExprDialect)
      Angular2EntitiesProvider.findTemplateComponent(file) != null
      || isHostBindingExpression(file)
    else
      super.isAcceptableForHighlighting(file)

  override fun getQuickInfoAt(usageElement: PsiElement, originalFile: VirtualFile): Future<TypeScriptQuickInfoResponse?>? =
    if (usageElement.containingFile.language.let { it is Angular2HtmlDialect || it is Angular2ExprDialect })
      null // For now do not use TS server for quick info
    else
      super.getQuickInfoAt(usageElement, originalFile)

  override suspend fun getCompletionItemsSuspending(virtualFile: VirtualFile, document: Document, offset: Int, parameters: CompletionParameters): List<TypeScriptService.CompletionEntry>? =
    if (!acceptCodeCompletionFromService(parameters))
      null // For now do not use TS server for code completions
    else {
      readActionWithSuspend { refreshTranspiledTemplateIfNeeded(virtualFile) }
      super.getCompletionItemsSuspending(virtualFile, document, offset, parameters)
    }

  override fun postprocessErrors(file: PsiFile, errors: List<JSAnnotationError>): List<JSAnnotationError> {
    val result = getTranspiledDirectiveAndTopLevelSourceFile(file)
                   ?.let { (transpiledDirectiveFile, topLevelFile) -> translateNamesInErrors(errors, transpiledDirectiveFile, topLevelFile) }
                 ?: errors
    return result.filter { Angular2LanguageServiceErrorFilter.accept(file, it) }
  }

  override fun getServiceFixes(file: PsiFile, element: PsiElement?, result: JSAnnotationError): Collection<IntentionAction> =
    super.getServiceFixes(file, element, result)
      .filter { Angular2LanguageServiceQuickFixFilter.accept(file, element, result, it) }

  override fun supportsInlayHints(file: PsiFile): Boolean =
    file.language is Angular2HtmlDialect || super.supportsInlayHints(file)


  @RequiresReadLock
  override suspend fun getInlayHints(file: PsiFile, textRange: TextRange): TypeScriptInlayHintsResult? = withScopedServiceTraceSpan("getInlayHintsAngular", myLifecycleSpan) {
    val hasTranspiledTemplate = refreshTranspiledTemplateIfNeeded(file.virtualFile
                                                                  ?: return@withScopedServiceTraceSpan null) != null
    val result = super.getInlayHints(file, textRange) ?: return@withScopedServiceTraceSpan null

    if (hasTranspiledTemplate)
      TypeScriptInlayHintsResult(repositionInlayHints(file, result.items), result.isSuccess, result.message)
    else
      result
  }

  private fun repositionInlayHints(file: PsiFile, hints: Array<InlayHintItem>): Array<InlayHintItem> = withServiceTraceSpan("repositionInlayHints") {
    val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return@withServiceTraceSpan hints
    return@withServiceTraceSpan hints.mapNotNull {
      transformInlayHints(file, document, it)
    }.toTypedArray()
  }

  override fun isTypeEvaluationEnabled(): Boolean = project.service<AngularSettings>().let {
    it.serviceType != AngularServiceSettings.DISABLED && it.useTypesFromServer
  }

  override val typeEvaluationSupport: Angular2TypeScriptServiceEvaluationSupport = Angular2CompilerServiceEvaluationSupport(project)

  override fun supportsTypeEvaluation(virtualFile: VirtualFile, element: PsiElement): Boolean =
    (element.language.let { it is Angular2ExprDialect || it is Angular2HtmlDialect }
     && Angular2EntitiesProvider.findTemplateComponent(element) != null
    ) || super.supportsTypeEvaluation(virtualFile, element)

  override fun isDisabledByContext(context: VirtualFile): Boolean =
    super.isDisabledByContext(context)
    || !isAngularServiceAvailableByContext(context)

  private fun isAngularServiceAvailableByContext(context: VirtualFile): Boolean =
    isAngularTypeScriptServiceEnabled(myProject, context)

  override fun createProtocol(tsServicePath: String): TypeScriptServiceStandardOutputProtocol =
    Angular2TypeScriptServiceProtocol(myProject, mySettings, createEventConsumer(), serviceName, tsServicePath)

  override fun createWidgetItem(currentFile: VirtualFile?): LanguageServiceWidgetItem =
    TypeScriptServiceWidgetItem(this, currentFile, AngularIcons.Angular2, AngularIcons.Angular2, AngularConfigurable::class.java)

  override fun createLSCache(): TypeScriptLanguageServiceCache =
    Angular2LanguageServiceCache(myProject)

  @RequiresReadLock
  override suspend fun beforeGetErrors(file: VirtualFile) {
    refreshTranspiledTemplateIfNeeded(file)
  }

  override fun isGeterrSupported(psiFile: PsiFile): Boolean {
    return psiFile.language.let { it !is Angular2ExprDialect && it !is Angular2HtmlDialect }
  }

  override fun skipInternalErrors(element: PsiElement): Boolean {
    return !isDeferOnReferenceExpression(element)
  }

  private suspend fun acceptCodeCompletionFromService(parameters: CompletionParameters): Boolean =
    readAction {
      val language = parameters.position.containingFile.language
      if (language !is Angular2HtmlDialect && language !is Angular2ExprDialect)
        return@readAction true
      // Support only string literals, where TS server should handle narrowing better
      return@readAction parameters.position.takeIf { it.elementType == JSTokenTypes.STRING_LITERAL }
        ?.parent?.asSafely<JSLiteralExpression>() != null
    }

  private fun translateNamesInErrors(errors: List<JSAnnotationError>, file: TranspiledDirectiveFile, templateFile: PsiFile): List<JSAnnotationError> = withServiceTraceSpan("translateNamesInErrors") {
    val document = PsiDocumentManager.getInstance(templateFile.project).getDocument(templateFile)
                   ?: return@withServiceTraceSpan emptyList()
    return@withServiceTraceSpan errors.map { error ->
      if (error is TypeScriptLanguageServiceAnnotationResult) {
        if (error.line < 0) return@map error
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

  private fun transformInlayHints(file: PsiFile, document: Document, hint: InlayHintItem): InlayHintItem? {
    if (hint.kind.let { it != InlayHintKind.Type && it != InlayHintKind.Parameter }) return hint
    val line = hint.position?.line ?: return hint
    val column = hint.position?.offset ?: return hint
    val offset = document.getLineStartOffset(line - 1) + column - 1
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
        hint.position!!.offset += textRange.endOffset - offset
        return hint
      }
      InlayHintKind.Parameter -> {
        val element = injectedElement ?: file.findElementAt(offset)
        return hint.takeIf { element !is XmlAttribute && element?.parent !is XmlAttribute }
      }
      else -> return hint
    }
  }

  private fun acceptElementToRepositionHint(element: PsiElement): Boolean =
    element is LeafPsiElement
    && element.containingFile.language.let { it is Angular2HtmlDialect || it is Angular2ExprDialect }

  @RequiresReadLock
  private suspend fun refreshTranspiledTemplateIfNeeded(virtualFile: VirtualFile): TranspiledDirectiveFile? = withScopedServiceTraceSpan("refreshTranspiledTemplateIfNeeded") {
    if (DumbService.isDumb(project)) return@withScopedServiceTraceSpan null
    // Updating the cache can cause the transpiled template to be (re)built,
    // so let's build the template first and ensure that it doesn't change
    // by keeping the read action lock. Otherwise, we can get unnecessary cancellations
    // on server cache locking leading to tests instability.
    val templatePsiFile = PsiManager.getInstance(project).findFile(virtualFile)
    val componentFile = templatePsiFile?.let { Angular2TranspiledDirectiveFileBuilder.findDirectiveFile(it) }
    val result = componentFile?.let { Angular2TranspiledDirectiveFileBuilder.getTranspiledDirectiveFile(it) }

    val componentVirtualFile = componentFile?.virtualFile
    if (componentVirtualFile != null) {
      val process = getProcess()
      if (process is TypeScriptLanguageServiceQueueImpl) {
        val cacheData = process.serverState.cacheData
        (cacheData as? Angular2LanguageServiceCache)?.refreshAndCacheTranspiledTemplate(process, componentVirtualFile, result)
      }
    }

    result
  }

  private inner class Angular2CompilerServiceEvaluationSupport(project: Project) : TypeScriptCompilerServiceEvaluationSupport(project),
                                                                                   Angular2TypeScriptServiceEvaluationSupport {

    override val service: TypeScriptService
      get() = this@Angular2TypeScriptService

    override fun getElementType(element: PsiElement, typeRequestKind: TypeScriptTypeRequestKind, virtualFile: VirtualFile, projectFile: VirtualFile?): TSType? =
      if (element !is JSElement && element.parent !is JSElement) null
      else super.getElementType(element, typeRequestKind, virtualFile, projectFile)

    override suspend fun commitDocuments(updateContext: UpdateContextInfo) {
      super.commitDocuments(updateContext)
      val element = updateContext.element
      if (element.language is Angular2ExprDialect || element.language is Angular2HtmlDialect) {
        refreshTranspiledTemplateIfNeeded(updateContext.file)
      }
    }

    override fun getGeneratedElementType(transpiledFile: TranspiledDirectiveFile, templateFile: PsiFile, generatedRange: TextRange): JSType? = withServiceTraceSpan("getGeneratedElementType", myLifecycleSpan) {
      ThreadingAssertions.assertReadAccess()
      val componentVirtualFile = transpiledFile.originalFile.originalFile.virtualFile
                                 ?: return@withServiceTraceSpan null
      val evaluationLocation = InjectedLanguageManager.getInstance(templateFile.project).getTopLevelFile(templateFile.originalFile).virtualFile
                               ?: return@withServiceTraceSpan null
      commitDocuments(componentVirtualFile)
      if (componentVirtualFile != evaluationLocation) {
        // If template is not inlined, we need to ensure that both component and template files are up-to-date
        commitDocuments(evaluationLocation)
      }
      // Ensure that transpiled template is up-to-date
      runBlockingCancellable {
        refreshTranspiledTemplateIfNeeded(componentVirtualFile)
      }
      ?: return@withServiceTraceSpan null

      val filePath = JSLanguageServiceUtil.awaitFuture(getFilePath(componentVirtualFile), JSLanguageServiceUtil.getShortTimeout())
                     ?: return@withServiceTraceSpan null

      // The evaluation location is in the template, so the config will be searched for the containing component file,
      // which is the transpiledFile.originalFile
      val projectFileName = TypeScriptConfigUtil.getConfigForPsiFile(transpiledFile.originalFile.originalFile)
        ?.configFile
        ?.let { JSLanguageServiceUtil.awaitFuture(getFilePath(it), JSLanguageServiceUtil.getShortTimeout()) }

      val range = Range(
        start = StringUtil.offsetToLineColumn(transpiledFile.generatedCode, generatedRange.startOffset).let { Position(it.line, it.column) },
        end = StringUtil.offsetToLineColumn(transpiledFile.generatedCode, generatedRange.endOffset).let { Position(it.line, it.column) }
      )

      val args = Angular2GetGeneratedElementTypeRequestArgs(filePath, projectFileName, range)
      return@withServiceTraceSpan sendGetElementTypeCommandAndDeserialize(
        transpiledFile.originalFile, null, Angular2GetGeneratedElementTypeCommand(args))?.toTSType()?.asJSType()
    }

    override fun isEnabledInUsageContext(usageContext: JSServicePoweredTypeEngineUsageContext): Boolean = true

    override val supportsTypeScriptInInjections: Boolean
      get() = true
  }

  private fun commitDocuments(virtualFile: VirtualFile) {
    update(createUpdateContext(virtualFile))
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

private fun isAngularServiceSupport(project: Project, context: VirtualFile): Boolean =
  PolyContext.get("angular-service-support", context, project) != "false"
