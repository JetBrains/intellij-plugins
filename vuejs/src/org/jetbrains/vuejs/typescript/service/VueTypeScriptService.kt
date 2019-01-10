// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.typescript.service

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceProtocol
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCacheImpl
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigureRequest
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigureRequestArguments
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.FileExtensionInfo
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.util.Consumer
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.codeInsight.findModule
import org.jetbrains.vuejs.index.hasVue
import org.jetbrains.vuejs.typescript.service.protocol.VueTypeScriptServiceProtocol

/**
 * We need to modify "original" file content by removing all content excluding ts code
 */
fun getModifiedVueDocumentText(project: Project, document: Document): String? {
  val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
  if (psiFile == null) return null

  val module = findModule(psiFile) ?: return null

  val text = module.node
  val textRange = text.textRange

  val lineNumber = document.getLineNumber(textRange.startOffset)
  val newLines = StringUtil.repeat("\n", lineNumber)
  val currentLineStart = textRange.startOffset - document.getLineStartOffset(lineNumber)

  if (currentLineStart < 0) return null

  val spacesCurrentLine = StringUtil.repeat(" ", currentLineStart)

  val startSpaceCount = textRange.startOffset - newLines.length - currentLineStart
  if (startSpaceCount < 0) return null
  val fakeBefore = StringUtil.repeat(" ", startSpaceCount)

  val afterSpaces = document.textLength - textRange.endOffset - 1
  if (afterSpaces < 0) return null

  val fakeAfter = StringUtil.repeat(" ", afterSpaces)

  val result = fakeBefore + newLines + spacesCurrentLine + text.text + "\n" + fakeAfter

  assert(result.length == document.textLength)

  return result
}

class VueTypeScriptService(project: Project, settings: TypeScriptCompilerSettings) :
  TypeScriptServerServiceImpl(project, settings, "Vue Console") {

  override fun isAcceptableNonTsFile(project: Project, service: TypeScriptConfigService, virtualFile: VirtualFile): Boolean {
    if (super.isAcceptableNonTsFile(project, service, virtualFile)) return true

    if (!isVueFile(virtualFile)) return false

    return service.getPreferableConfig(virtualFile) != null
  }

  override fun getProcessName(): String = "Vue TypeScript"

  override fun isServiceEnabled(context: VirtualFile): Boolean = super.isServiceEnabled(context) && isVueServiceEnabled()

  override fun createProtocol(readyConsumer: Consumer<*>, tsServicePath: String): JSLanguageServiceProtocol? {
    return VueTypeScriptServiceProtocol(myProject, mySettings, readyConsumer, createEventConsumer(), tsServicePath)
  }

  private fun isVueServiceEnabled(): Boolean = hasVue(myProject)

  override fun getInitialCommands(): Map<JSLanguageServiceSimpleCommand, Consumer<JSLanguageServiceObject>> {
    //commands
    val initialCommands = super.getInitialCommands()
    val result: MutableMap<JSLanguageServiceSimpleCommand, Consumer<JSLanguageServiceObject>> = ContainerUtil.newLinkedHashMap()
    addConfigureCommand(result)

    result.putAll(initialCommands)
    return result
  }

  override fun canHighlight(file: PsiFile): Boolean {
    if (super.canHighlight(file)) return true

    val fileType = file.fileType
    if (fileType != VueFileType.INSTANCE) return false

    val virtualFile = file.virtualFile ?: return false

    if (!isServiceEnabled(virtualFile)) return false

    val configForFile = getConfigForFile(virtualFile)

    return configForFile != null
  }

  private fun addConfigureCommand(result: MutableMap<JSLanguageServiceSimpleCommand, Consumer<JSLanguageServiceObject>>) {
    val arguments = ConfigureRequestArguments()
    val fileExtensionInfo = FileExtensionInfo()
    fileExtensionInfo.extension = ".vue"
    arguments.extraFileExtensions = arrayOf(fileExtensionInfo)

    result[ConfigureRequest(arguments)] = Consumer {}
  }

  override fun createLSCache(): TypeScriptLanguageServiceCacheImpl {
    return object : TypeScriptLanguageServiceCacheImpl(myProject) {
      override fun getDocumentText(virtualFile: VirtualFile, document: Document): String {
        if (!isVueFile(virtualFile)) {
          return super.getDocumentText(virtualFile, document)
        }

        return getModifiedVueDocumentText(myProject, document) ?: ""
      }
    }
  }

  override fun getDocumentText(file: VirtualFile, instance: FileDocumentManager, document: Document): String? {
    if (!isVueFile(file)) return super.getDocumentText(file, instance, document)

    return getModifiedVueDocumentText(myProject, document)
  }

  private fun isVueFile(virtualFile: VirtualFile) = virtualFile.fileType == VueFileType.INSTANCE

}