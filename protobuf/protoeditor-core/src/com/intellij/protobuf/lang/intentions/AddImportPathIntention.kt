package com.intellij.protobuf.lang.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.protobuf.ide.settings.PbProjectSettings.ImportPathEntry
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbImportStatement
import com.intellij.protobuf.lang.util.PbImportPathResolver
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.util.IncorrectOperationException

class AddImportPathIntention : IntentionAction {
  override fun getText(): String {
    return "Add import path" //todo extract  to bundle
  }

  override fun getFamilyName(): String {
    return "Add import path"
  }

  override fun isAvailable(project: Project,
                           editor: Editor,
                           file: PsiFile): Boolean {
    return runReadAction {
      if (DumbService.isDumb(project) || file !is PbFile)
        false
      else
        findEditedImportStatement(editor, file)?.endsWith(".proto") ?: false // work only with files, skip incomplete/empty paths
    }
  }

  @Throws(IncorrectOperationException::class)
  override fun invoke(project: Project, editor: Editor, file: PsiFile) {
    val importStatement = findEditedImportStatement(editor, file) ?: return
    val suggestedImportPaths = PbImportPathResolver.findSuitableImportPaths(importStatement, project)
      .map { ImportPathEntry(FileUtil.toSystemDependentName(it), "") }

    //todo display popup if more than 1 variant is present
    val projectSettings = PbProjectSettings.getInstance(project)
    projectSettings.importPathEntries = projectSettings.importPathEntries + suggestedImportPaths
    PbProjectSettings.notifyUpdated(project)
  }

  override fun startInWriteAction(): Boolean {
    return false
  }

  private fun findEditedImportStatement(editor: Editor,
                                        file: PsiFile): String? {
    val offset = editor.caretModel.offset
    return file.findElementAt(offset)
      ?.parentOfType<PbImportStatement>(true)
      ?.importName?.stringValue?.value
  }
}