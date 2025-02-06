// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.classic

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.service.JSLanguageServiceAnnotationResult
import com.intellij.lang.javascript.service.JSLanguageServiceFileCommandCache
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceProtocol
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceAnnotationResult
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceWidgetItem
import com.intellij.lang.typescript.compiler.languageService.codeFixes.TypeScriptLanguageServiceFixSet
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigureRequest
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigureRequestArguments
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.FileExtensionInfo
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.index.VUE_FILE_EXTENSION
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.lang.typescript.service.isVueClassicTypeScriptServiceEnabled
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspTypeScriptService
import org.jetbrains.vuejs.options.VueConfigurable
import java.util.function.Consumer

/**
 * Original [TypeScriptService] implementation for Vue.
 * Superseded by integration with [Vue Language Tools (ex-Volar)][VueLspTypeScriptService] through LSP.
 *
 * Not used anymore by default but can be toggled in Settings for people that have problems with Vue LS, particularly some Vue 2.x users;
 * therefore, it's not reasonable to delete it.
 *
 * Provides limited features through TSServer protocol with a custom TS plugin that rewrites `.vue` files into valid `.ts` files.
 *
 * Doesn't work properly with TypeScript 5+ and is known to have some bugs.
 */
class VueClassicTypeScriptService(project: Project) : TypeScriptServerServiceImpl(project, "Vue Console") {

  override fun getProcessName(): String = "Vue TypeScript"

  override fun isDisabledByContext(context: VirtualFile): Boolean {
    if (super.isDisabledByContext(context)) return true

    return !isVueClassicTypeScriptServiceEnabled(myProject, context)
  }

  override fun isAcceptableNonTsFile(project: Project, service: TypeScriptConfigService, virtualFile: VirtualFile): Boolean {
    if (super.isAcceptableNonTsFile(project, service, virtualFile)) return true
    if (!virtualFile.isVueFile) return false

    return service.getDirectIncludePreferableConfig(virtualFile) != null
  }

  override fun createProtocol(readyConsumer: Consumer<*>, tsServicePath: String): JSLanguageServiceProtocol {
    return VueTypeScriptServiceProtocol(myProject, mySettings, readyConsumer, createEventConsumer(), tsServicePath)
  }

  override fun getInitialOpenCommands(): List<JSLanguageServiceSimpleCommand> {
    return listOf(createConfigureCommand()) + super.getInitialOpenCommands()
  }

  private fun createConfigureCommand(): JSLanguageServiceSimpleCommand {
    val arguments = ConfigureRequestArguments("IntelliJ")
    val fileExtensionInfo = FileExtensionInfo()
    fileExtensionInfo.extension = VUE_FILE_EXTENSION

    //see ts.getSupportedExtensions
    //x.scriptKind === ScriptKind.Deferred(7) || needJsExtensions && isJSLike(x.scriptKind) ? x.extension : undefined
    //so only "ScriptKind.Deferred" kinds are accepted for file searching
    fileExtensionInfo.scriptKind = 7

    fileExtensionInfo.isMixedContent = false
    arguments.extraFileExtensions = arrayOf(fileExtensionInfo)

    return ConfigureRequest(arguments)
  }

  override fun postprocessErrors(file: PsiFile, errors: List<JSAnnotationError>): List<JSAnnotationError> {
    if (file.virtualFile?.isVueFile == true) {
      val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return emptyList()
      val regularModuleRangeFilter = getRangeFilter(file, false, document)
      val scriptModuleRangeFilter = getRangeFilter(file, true, document)
      return errors.filter {
        it is JSLanguageServiceAnnotationResult && (
          regularModuleRangeFilter?.invoke(it) == true || (scriptModuleRangeFilter?.invoke(it) == true && !skipScriptSetupError(it)))
      }
    }
    else
      return errors
  }

  /**
   * Transformation from .vue to .ts is imperfect & causes false errors, which we then filter on the IDE side.
   *
   * Error codes taken from `src/compiler/diagnosticMessages.json` in TypeScript repository.
   */
  private val suppressedErrorCodes = setOf(
    1232, // An import declaration can only be used in a namespace or module.
    6192, // All imports in import declaration are unused.
    6133, // '{0}' is declared but its value is never read.
    6196, // '{0}' is declared but never used.
    6198, // All destructured elements are unused.
    6199, // All variables are unused.
    1184, // Modifiers cannot appear here. // Actually covers both type and value declarations, TODO reimplement only for value declarations as an inspection
    2614, // Module '{0}' has no exported member '{1}' // TODO consider implementing WEB-54985 or something different, then remove this suppression
  )

  private fun skipScriptSetupError(error: JSLanguageServiceAnnotationResult): Boolean {
    return error is TypeScriptLanguageServiceAnnotationResult && suppressedErrorCodes.contains(error.errorCode)
  }

  private fun getRangeFilter(file: PsiFile, setup: Boolean, document: Document): ((JSLanguageServiceAnnotationResult) -> Boolean)? {
    val module = findModule(file, setup)?.takeIf { DialectDetector.isTypeScript(it) } ?: return null
    val startOffset = module.textRange.startOffset
    val startLine = document.getLineNumber(startOffset)
    val startColumn = startOffset - document.getLineStartOffset(startLine)
    val endOffset = module.textRange.endOffset
    val endLine = document.getLineNumber(endOffset)
    val endColumn = endOffset - document.getLineStartOffset(endLine)
    return { error -> isWithinRange(error, startLine, startColumn, endLine, endColumn) }
  }

  private fun isWithinRange(
    error: JSLanguageServiceAnnotationResult,
    startLine: Int,
    startColumn: Int,
    endLine: Int,
    endColumn: Int,
  ): Boolean =
    (error.line > startLine || error.line == startLine && error.column >= startColumn) &&
    (error.endLine < endLine || error.endLine == endLine && error.endColumn <= endColumn)

  override fun isAcceptableForHighlighting(file: PsiFile): Boolean {
    if (!file.isVueFile) return false
    val virtualFile = file.virtualFile ?: return false

    if (findModule(file, false)?.let { DialectDetector.isTypeScript(it) } != true
        && findModule(file, true)?.let { DialectDetector.isTypeScript(it) } != true)
      return false

    val configForFile = getConfigForFile(virtualFile)

    return configForFile != null
  }

  override fun skipInternalErrors(element: PsiElement): Boolean {
    val context = PsiTreeUtil.getParentOfType(element, VueJSEmbeddedExpressionContent::class.java, JSEmbeddedContent::class.java)
    return context !is VueJSEmbeddedExpressionContent
  }

  override fun createFixSet(
    file: PsiFile,
    cache: JSLanguageServiceFileCommandCache,
    typescriptResult: TypeScriptLanguageServiceAnnotationResult,
  ): TypeScriptLanguageServiceFixSet {
    if (file.isVueFile) {
      val textRanges = mutableListOf<TextRange>()
      findModule(file, true)?.let { textRanges.add(it.textRange) }
      findModule(file, false)?.let { textRanges.add(it.textRange) }
      return TypeScriptLanguageServiceFixSet(file.project, cache, file.virtualFile, typescriptResult, textRanges)
    }

    return super.createFixSet(file, cache, typescriptResult)
  }

  override fun createWidgetItem(currentFile: VirtualFile?): LanguageServiceWidgetItem =
    TypeScriptServiceWidgetItem(this, currentFile, VuejsIcons.Vue, VuejsIcons.Vue, VueConfigurable::class.java)
}
