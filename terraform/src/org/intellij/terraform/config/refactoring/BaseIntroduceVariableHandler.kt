// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring

import com.intellij.featureStatistics.FeatureUsageTracker
import com.intellij.featureStatistics.ProductivityFeatureNames
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.util.CommonRefactoringUtil
import org.intellij.terraform.hcl.HCLBundle
import org.jetbrains.annotations.Nls

abstract class BaseIntroduceVariableHandler<T : PsiElement> : RefactoringActionHandler {
  companion object {
    private fun showErrorMessage(project: Project, editor: Editor?, @Nls message: String) {
      CommonRefactoringUtil.showErrorHint(project, editor, message, HCLBundle.message("introduce.variable.title"), "refactoring.extractMethod")
    }

    fun showCannotPerformError(project: Project, editor: Editor) {
      showErrorMessage(project, editor, HCLBundle.message("refactoring.introduce.selection.error"))
    }
  }

  override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
    throw UnsupportedOperationException()
  }

  override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext?) {
    if (editor == null || file == null || dataContext == null) return
    FeatureUsageTracker.getInstance().triggerFeatureUsed(ProductivityFeatureNames.REFACTORING_INTRODUCE_VARIABLE)
    PsiDocumentManager.getInstance(project).commitAllDocuments()
    performAction(createOperation(editor, file, project))
  }

  protected abstract fun createOperation(editor: Editor, file: PsiFile, project: Project): BaseIntroduceOperation<T>

  abstract fun performAction(operation: BaseIntroduceOperation<T>)
}
