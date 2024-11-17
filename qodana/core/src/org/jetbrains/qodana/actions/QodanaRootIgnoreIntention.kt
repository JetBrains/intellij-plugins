package org.jetbrains.qodana.actions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.qodana.QodanaBundle

class QodanaRootIgnoreIntention : IntentionAction {
  override fun startInWriteAction(): Boolean = false

  override fun getText(): String = QodanaBundle.message("qodana.intentions.ignore.text")

  override fun getFamilyName(): String = QodanaBundle.message("qodana.intentions.family.name")

  override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

  override fun invoke(project: Project, editor: Editor?, file: PsiFile?) { }
}