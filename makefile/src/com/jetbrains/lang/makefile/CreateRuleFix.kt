package com.jetbrains.lang.makefile

import com.intellij.codeInsight.intention.impl.*
import com.intellij.openapi.command.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import com.jetbrains.lang.makefile.psi.*

class CreateRuleFix(private val prerequisite: PsiElement) : BaseIntentionAction() {
  override fun getText() = MakefileLangBundle.message("intention.name.create.rule")
  override fun getFamilyName() = MakefileLangBundle.message("intention.family.name.create.rule")

  override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?) = true

  override fun invoke(project: Project, editor: Editor?, psiFile: PsiFile?) {
    WriteCommandAction.runWriteCommandAction(project) {
      val file = psiFile as MakefileFile
      val rule = MakefileElementFactory.createRule(project, prerequisite.text)
      val anchor = prerequisite.parent.parent.parent.parent.nextSibling?.node
      file.node.addChild(MakefileElementFactory.createEOL(project, "\n").node, anchor)
      file.node.addChild(rule.node, anchor)
      file.node.addChild(MakefileElementFactory.createEOL(project, "\n").node, anchor)
      (rule.lastChild.navigationElement as Navigatable).navigate(true)
    }
  }
}