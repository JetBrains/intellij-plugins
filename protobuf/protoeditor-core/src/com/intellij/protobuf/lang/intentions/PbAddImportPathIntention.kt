package com.intellij.protobuf.lang.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.command.undo.UndoableAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.protobuf.lang.PbLangBundle
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbImportStatement
import com.intellij.protobuf.lang.util.PbImportPathResolver
import com.intellij.protobuf.lang.util.PbUiUtils
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.util.IncorrectOperationException

class PbAddImportPathIntention : IntentionAction {
  override fun getText(): String {
    return PbLangBundle.message("intention.add.import.path.name")
  }

  override fun getFamilyName(): String {
    return PbLangBundle.message("intention.fix.import.problems.familyName")
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
    val suggestedImportPaths = PbImportPathResolver.findSuitableImportPaths(importStatement, file.virtualFile, project)

    PbUiUtils.selectItemAndApply(suggestedImportPaths, editor, project) { pathToAdd ->
      WriteCommandAction.runWriteCommandAction(
        project,
        PbLangBundle.message("intention.add.import.path.popup.title"),
        PbLangBundle.message("intention.fix.import.problems.familyName"),
        {
          UndoManager.getInstance(project).undoableActionPerformed(
            PbAddImportPathUndoableAction(pathToAdd, project).also(UndoableAction::redo)
          )
        }
      )
    }
  }

  override fun startInWriteAction(): Boolean {
    return false
  }

  private fun findEditedImportStatement(editor: Editor, file: PsiFile): String? {
    val offset = editor.caretModel.offset
    return file.findElementAt(offset)
      ?.parentOfType<PbImportStatement>(true)
      ?.importName?.stringValue?.value
  }
}