// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.service.JSLanguageServiceAnnotationResult
import com.intellij.lang.javascript.service.JSLanguageServiceFileCommandCache
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceProtocol
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceAnnotationResult
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.compiler.languageService.codeFixes.TypeScriptLanguageServiceFixSet
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigureRequest
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigureRequestArguments
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.FileExtensionInfo
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.util.Consumer
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.index.VUE_FILE_EXTENSION
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.typescript.service.protocol.VueTypeScriptServiceProtocol

class VueTypeScriptService(project: Project) : TypeScriptServerServiceImpl(project, "Vue Console") {

  override fun isAcceptableNonTsFile(project: Project, service: TypeScriptConfigService, virtualFile: VirtualFile): Boolean {
    if (super.isAcceptableNonTsFile(project, service, virtualFile)) return true
    if (!isVueFile(virtualFile)) return false

    return service.getDirectIncludePreferableConfig(virtualFile) != null
  }

  override fun postprocessErrors(file: PsiFile, errors: List<JSAnnotationError>): List<JSAnnotationError> {
    if (file.virtualFile != null && isVueFile(file.virtualFile)) {
      return ReadAction.compute<List<JSAnnotationError>, Throwable> {
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return@compute emptyList()
        val regularModuleRangeFilter = getRangeFilter(file, false, document)
        val scriptModuleRangeFilter = getRangeFilter(file, true, document)
        return@compute errors.filter {
          it is JSLanguageServiceAnnotationResult && (
            regularModuleRangeFilter?.invoke(it) == true || (scriptModuleRangeFilter?.invoke(it) == true && !skipScriptSetupError(it)))
        }
      }
    }
    return super.postprocessErrors(file, errors)
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

  private fun isWithinRange(error: JSLanguageServiceAnnotationResult,
                            startLine: Int,
                            startColumn: Int,
                            endLine: Int,
                            endColumn: Int): Boolean =
    (error.line > startLine || error.line == startLine && error.column >= startColumn) &&
    (error.endLine < endLine || error.endLine == endLine && error.endColumn <= endColumn)

  override fun getProcessName(): String = "Vue TypeScript"

  override fun isDisabledByContext(context: VirtualFile): Boolean {
    if (super.isDisabledByContext(context)) return true
    if (context.fileType is VueFileType) return false

    //other files
    return !isVueContext(context, myProject)
  }

  override fun createProtocol(readyConsumer: Consumer<*>, tsServicePath: String): JSLanguageServiceProtocol {
    return VueTypeScriptServiceProtocol(myProject, mySettings, readyConsumer, createEventConsumer(), tsServicePath)
  }

  override fun getInitialCommands(): Map<JSLanguageServiceSimpleCommand, Consumer<JSLanguageServiceObject>> {
    //commands
    val initialCommands = super.getInitialCommands()
    val result: MutableMap<JSLanguageServiceSimpleCommand, Consumer<JSLanguageServiceObject>> = linkedMapOf()
    addConfigureCommand(result)

    result.putAll(initialCommands)
    return result
  }

  override fun canHighlight(file: PsiFile): Boolean {
    if (super.canHighlight(file)) return true

    val fileType = file.fileType
    if (fileType != VueFileType.INSTANCE) return false

    val virtualFile = file.virtualFile ?: return false

    if (isDisabledByContext(virtualFile) || !checkAnnotationProvider(file)) return false

    if (findModule(file, false)?.let { DialectDetector.isTypeScript(it) } != true
        && findModule(file, true)?.let { DialectDetector.isTypeScript(it) } != true)
      return false

    val configForFile = getConfigForFile(virtualFile)

    return configForFile != null
  }

  private fun addConfigureCommand(result: MutableMap<JSLanguageServiceSimpleCommand, Consumer<JSLanguageServiceObject>>) {
    val arguments = ConfigureRequestArguments()
    val fileExtensionInfo = FileExtensionInfo()
    fileExtensionInfo.extension = VUE_FILE_EXTENSION

    //see ts.getSupportedExtensions
    //x.scriptKind === ScriptKind.Deferred(7) || needJsExtensions && isJSLike(x.scriptKind) ? x.extension : undefined
    //so only "ScriptKind.Deferred" kinds are accepted for file searching
    fileExtensionInfo.scriptKind = 7

    fileExtensionInfo.isMixedContent = false
    arguments.extraFileExtensions = arrayOf(fileExtensionInfo)

    result[ConfigureRequest(arguments)] = Consumer {}
  }

  override fun createFixSet(file: PsiFile,
                            cache: JSLanguageServiceFileCommandCache,
                            typescriptResult: TypeScriptLanguageServiceAnnotationResult): TypeScriptLanguageServiceFixSet {
    if (isVueFile(file.virtualFile)) {
      val textRanges = mutableListOf<TextRange>()
      findModule(file, true)?.let { textRanges.add(it.textRange) }
      findModule(file, false)?.let { textRanges.add(it.textRange) }
      return TypeScriptLanguageServiceFixSet(file.project, cache, file.virtualFile, typescriptResult, textRanges)
    }

    return super.createFixSet(file, cache, typescriptResult)
  }

  private fun isVueFile(virtualFile: VirtualFile) = FileTypeRegistry.getInstance().isFileOfType(virtualFile, VueFileType.INSTANCE)

}
