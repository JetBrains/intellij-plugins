package name.kropp.intellij.makefile

import com.intellij.codeInsight.intention.impl.*
import com.intellij.openapi.command.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import name.kropp.intellij.makefile.psi.*

class RemoveRuleFix(private val rule: MakefileRule) : BaseIntentionAction() {
  override fun getText() = "Remove Empty Rule"
  override fun getFamilyName() = "Remove Empty Rule"

  override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?) = true

  override fun invoke(project: Project, editor: Editor?, psiFile: PsiFile?) {
    WriteCommandAction.runWriteCommandAction(project) {
      rule.delete()
    }
  }
}