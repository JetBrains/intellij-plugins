package com.intellij.protobuf.lang.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.protobuf.lang.PbLangBundle
import com.intellij.protobuf.lang.intentions.util.PbImportPathResolver
import com.intellij.protobuf.lang.intentions.util.selectItemAndApply
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbImportStatement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.util.IncorrectOperationException

class PbAddImportPathIntention : IntentionAction {

  override fun generatePreview(project: Project, editor: Editor, psiFile: PsiFile): IntentionPreviewInfo {
    return IntentionPreviewInfo.EMPTY
  }

  override fun getText(): String {
    return PbLangBundle.message("intention.add.import.path.name")
  }

  override fun getFamilyName(): String {
    return PbLangBundle.message("intention.fix.import.problems.familyName")
  }

  override fun isAvailable(project: Project,
                           editor: Editor,
                           psiFile: PsiFile): Boolean {
    return runReadAction {
      if (DumbService.isDumb(project) || psiFile !is PbFile)
        false
      else
        findEditedImportStatement(editor, psiFile)?.endsWith(".proto") ?: false // work only with files, skip incomplete/empty paths
    }
  }

  @Throws(IncorrectOperationException::class)
  override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
    if (ApplicationManager.getApplication().isUnitTestMode) {
      selectItemAndApply(prepareQuickFixes(project, editor, psiFile), editor, project)
      return
    }

    ProgressManager.getInstance().runProcessWithProgressSynchronously(
      {
        val fixes = runReadAction { prepareQuickFixes(project, editor, psiFile) }
        selectItemAndApply(fixes, editor, project)
      },
      PbLangBundle.message("background.task.title.add.import.prepare.variants"),
      true,
      project
    )
  }

  private fun prepareQuickFixes(project: Project, editor: Editor, file: PsiFile): List<PbImportIntentionVariant> {
    val importStatement = findEditedImportStatement(editor, file) ?: return emptyList()
    return PbImportPathResolver.findSuitableImportPaths(importStatement, file.virtualFile, project)
      .map(PbImportIntentionVariant::AddImportPathToSettings)
      .plus(PbImportIntentionVariant.ManuallyConfigureImportPathsSettings)
      .toList()
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