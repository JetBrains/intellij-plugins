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
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCache
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigureRequest
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigureRequestArguments
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.FileExtensionInfo
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptOpenEditorCommand
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.util.Consumer
import org.jetbrains.vuejs.context.enableVueTSService
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.typescript.service.protocol.VueTypeScriptServiceProtocol

class VueTypeScriptService(project: Project) : TypeScriptServerServiceImpl(project, "Vue Console") {

  override fun isAcceptableNonTsFile(project: Project, service: TypeScriptConfigService, virtualFile: VirtualFile): Boolean {
    if (super.isAcceptableNonTsFile(project, service, virtualFile)) return true
    if (!isVueFile(virtualFile)) return false

    //lightweight check -> only parent configs, no indirect deps
    val configs = TypeScriptConfigUtil.getNearestParentConfigFiles(virtualFile, service.configFiles)
    return configs.any { service.parseConfigFile(it)?.include?.accept(virtualFile) ?: false }
  }

  override fun postprocessErrors(file: PsiFile, errors: MutableList<JSAnnotationError>): List<JSAnnotationError> {
    if (file.virtualFile != null && isVueFile(file.virtualFile)) {
      return ReadAction.compute<List<JSAnnotationError>, Throwable> {
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file)
        val module = findModule(file)
        if (module != null && document != null) {
          val startOffset = module.textRange.startOffset
          val startLine = document.getLineNumber(startOffset)
          val startColumn = startOffset - document.getLineStartOffset(startLine)
          val endOffset = module.textRange.endOffset
          val endLine = document.getLineNumber(endOffset)
          val endColumn = endOffset - document.getLineStartOffset(endLine)
          return@compute errors.filter { error -> isWithinRange(error, startLine, startColumn, endLine, endColumn) }
        }
        return@compute super.postprocessErrors(file, errors)
      }
    }
    return super.postprocessErrors(file, errors)
  }

  private fun isWithinRange(error: JSAnnotationError, startLine: Int, startColumn: Int, endLine: Int, endColumn: Int): Boolean {
    if (error !is JSLanguageServiceAnnotationResult) {
      return false
    }
    return (error.line > startLine || error.line == startLine && error.column >= startColumn) &&
           (error.endLine < endLine || error.endLine == endLine && error.endColumn <= endColumn)
  }

  override fun getProcessName(): String = "Vue TypeScript"

  override fun isServiceEnabled(context: VirtualFile): Boolean = super.isServiceEnabled(context) && isVueServiceEnabled()

  override fun createProtocol(readyConsumer: Consumer<*>, tsServicePath: String): JSLanguageServiceProtocol? {
    return VueTypeScriptServiceProtocol(myProject, mySettings, readyConsumer, createEventConsumer(), tsServicePath)
  }

  private fun isVueServiceEnabled(): Boolean = enableVueTSService(myProject)

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

    if (!isServiceEnabled(virtualFile) || !checkAnnotationProvider(file)) return false

    val module = findModule(file)
    if (module == null || !DialectDetector.isTypeScript(module)) return false

    val configForFile = getConfigForFile(virtualFile)

    return configForFile != null
  }

  private fun addConfigureCommand(result: MutableMap<JSLanguageServiceSimpleCommand, Consumer<JSLanguageServiceObject>>) {
    val arguments = ConfigureRequestArguments()
    val fileExtensionInfo = FileExtensionInfo()
    fileExtensionInfo.extension = ".vue"
    fileExtensionInfo.scriptKind = 3
    fileExtensionInfo.isMixedContent = false
    arguments.extraFileExtensions = arrayOf(fileExtensionInfo)

    result[ConfigureRequest(arguments)] = Consumer {}
  }

  override fun createLSCache(): TypeScriptLanguageServiceCache {
    return VueTypeScriptServiceCache(myProject)
  }

  override fun createFixSet(file: PsiFile,
                            cache: JSLanguageServiceFileCommandCache,
                            typescriptResult: TypeScriptLanguageServiceAnnotationResult): TypeScriptLanguageServiceFixSet {
    val textRange = findModule(file)?.textRange
    return TypeScriptLanguageServiceFixSet(file.project, cache, file.virtualFile, typescriptResult, textRange)
  }

  private fun isVueFile(virtualFile: VirtualFile) = virtualFile.fileType == VueFileType.INSTANCE

}
