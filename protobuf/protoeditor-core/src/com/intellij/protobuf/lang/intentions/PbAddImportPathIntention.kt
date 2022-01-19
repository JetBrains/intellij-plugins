package com.intellij.protobuf.lang.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.protobuf.ide.settings.ProjectSettingsConfiguratorManager
import com.intellij.protobuf.lang.PbLangBundle
import com.intellij.protobuf.lang.intentions.util.PbUiUtils
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbImportStatement
import com.intellij.protobuf.lang.util.PbImportPathResolver
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.util.IncorrectOperationException
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.Callable

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
    if (ApplicationManager.getApplication().isUnitTestMode) {
      PbUiUtils.selectItemAndApply(prepareQuickFixes(project, editor, file), editor, project)
      return
    }

    ReadAction.nonBlocking(Callable { prepareQuickFixes(project, editor, file) })
      .expireWith(ProjectSettingsConfiguratorManager.getInstance(project))
      .inSmartMode(project)
      .coalesceBy(editor, file)
      .finishOnUiThread(ModalityState.NON_MODAL) { fixes ->
        PbUiUtils.selectItemAndApply(fixes, editor, project)
      }
      .submit(AppExecutorUtil.getAppExecutorService());
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