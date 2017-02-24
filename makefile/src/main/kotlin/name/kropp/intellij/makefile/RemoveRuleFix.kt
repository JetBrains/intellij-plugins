package name.kropp.intellij.makefile

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import name.kropp.intellij.makefile.psi.MakefileRule

class RemoveRuleFix(private val rule: MakefileRule) : BaseIntentionAction() {
  override fun getText() = "Remove Empty Rule"
  override fun getFamilyName() = "Remove Empty Rule"

  override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?) = true

  override fun invoke(project: Project, editor: Editor?, psiFile: PsiFile?) {
    object : WriteCommandAction.Simple<Any>(project, psiFile) {
      override fun run() {
        rule.delete()
      }
    }.execute()
  }
}