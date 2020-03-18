// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCache
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptOpenEditorCommand
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.lang.html.VueFileType

class VueTypeScriptServiceCache(project: Project) : TypeScriptLanguageServiceCache(project) {
  companion object {

    fun createOpenCommand(project: Project,
                          virtualFile: VirtualFile,
                          timestamp: Long,
                          contentLength: Long,
                          lineCount: Int,
                          lastLineStartOffset: Int,
                          content: CharSequence?,
                          projectFileName: String?): TypeScriptOpenEditorCommand {
      if (virtualFile.fileType == VueFileType.INSTANCE) {
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
        if (psiFile != null) {
          val module = findModule(psiFile)
          if (module != null) {
            val holder = DialectDetector.dialectOfElement(module)
            if (holder != null && holder.isTSX) {
              return TypeScriptOpenEditorCommand(virtualFile, timestamp, contentLength, lineCount, lastLineStartOffset, content, projectFileName, "TSX")
            }
          }
        }
      }
      
      return TypeScriptOpenEditorCommand(virtualFile, timestamp, contentLength, lineCount, lastLineStartOffset, content, projectFileName)
    }

  }

  override fun getDocumentText(virtualFile: VirtualFile, document: Document): CharSequence {
    return if (virtualFile.fileType != VueFileType.INSTANCE) {
      super.getDocumentText(virtualFile, document)
    }
    else getModifiedVueDocumentText(myProject, document) ?: ""
  }

  override fun createOpenCommand(virtualFile: VirtualFile,
                                 info: LastUpdateInfo,
                                 text: CharSequence?,
                                 projectFileName: String?,
                                 timestamp: Long): TypeScriptOpenEditorCommand? {
    return createOpenCommand(myProject, virtualFile, timestamp, info.myContentLength, info.myLineCount,
                             info.myLastLineStartOffset, text, projectFileName)
  }

  override fun getFilesToClose(currentChangedFiles: MutableMap<VirtualFile, Long>): Set<VirtualFile> {
    val filesToClose: Set<VirtualFile> = super.getFilesToClose(currentChangedFiles)

    val toCloseByChangedType = mutableSetOf<VirtualFile>()
    currentChangedFiles.forEach { (file, _) -> addFileIfInvalid(file, filesToClose, toCloseByChangedType) }
    myOpenedFilesByEvent.forEach { file -> addFileIfInvalid(file, filesToClose, toCloseByChangedType) }

    if (toCloseByChangedType.isNotEmpty()) {
      myOpenedFilesByEvent.removeAll(toCloseByChangedType)
      return filesToClose + toCloseByChangedType
    }

    return filesToClose
  }

  private fun addFileIfInvalid(file: VirtualFile,
                               filesToClose: Set<VirtualFile>,
                               toCloseByChangedType: MutableSet<VirtualFile>) {
    if (isInvalidVueTypeScriptFile(file) && !filesToClose.contains(file)) toCloseByChangedType.add(file)
  }

  private fun isInvalidVueTypeScriptFile(file: VirtualFile): Boolean {
    if (file.isValid) {
      val fileType = file.fileType
      if (fileType == VueFileType.INSTANCE) {
        val findFile = PsiManager.getInstance(myProject).findFile(file)
        if (findFile == null) return false

        val module = findModule(findFile)
        return module == null || !DialectDetector.isTypeScript(module)
      }
    }
    return false
  }
}
