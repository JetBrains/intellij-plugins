package name.kropp.intellij.makefile

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import name.kropp.intellij.makefile.psi.MakefileElementFactory

class CreateRuleFix(private val prerequisite: PsiElement) : BaseIntentionAction() {
  override fun getText() = "Create Rule"
  override fun getFamilyName() = "Create Rule"

  override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?) = true

  override fun invoke(project: Project, editor: Editor?, psiFile: PsiFile?) {
    object : WriteCommandAction.Simple<Any>(project, psiFile) {
      override fun run() {
        val file = psiFile as MakefileFile
        val rule = MakefileElementFactory.createRule(project, prerequisite.text)
        val anchor = prerequisite.parent.parent.parent.parent.nextSibling?.node
        file.node.addChild(MakefileElementFactory.createWhiteSpace(project, "\n\n").node, anchor)
        file.node.addChild(rule.node, anchor)
        (rule.lastChild.navigationElement as Navigatable).navigate(true)
      }
    }.execute()
  }
}