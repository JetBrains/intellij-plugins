package com.jetbrains.lang.makefile.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.lang.makefile.MakefileFile
import com.jetbrains.lang.makefile.MakefileLangBundle
import com.jetbrains.lang.makefile.psi.MakefileElementFactory

class CreateRuleFix : LocalQuickFix {
  override fun getName(): String =
    MakefileLangBundle.message("intention.name.create.rule")

  override fun getFamilyName(): String =
    MakefileLangBundle.message("intention.family.name.create.rule")

  override fun startInWriteAction(): Boolean =
    true

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val prerequisite = descriptor.psiElement
    val file = prerequisite.containingFile as? MakefileFile ?: return
    val rule = MakefileElementFactory.createRule(project, prerequisite.text)
    val anchor = prerequisite.parent.parent.parent.parent.nextSibling?.node
    file.node.addChild(MakefileElementFactory.createEOL(project, "\n").node, anchor)
    file.node.addChild(rule.node, anchor)
    file.node.addChild(MakefileElementFactory.createEOL(project, "\n").node, anchor)
  }
}