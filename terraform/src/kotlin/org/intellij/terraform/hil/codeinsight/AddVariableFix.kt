// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.codeinsight

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.util.CommonRefactoringUtil
import org.intellij.terraform.config.psi.TerraformElementGenerator
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hil.refactoring.ILIntroduceVariableHandler

class AddVariableFix(element: PsiNamedElement, private val vName: String = element.name!!) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
  override fun getFamilyName() = HCLBundle.message("AddVariableFix.family.name")
  override fun getText() = HCLBundle.message("AddVariableFix.text", vName)
  override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
    val declaration = TerraformElementGenerator(project).createVariable(vName, null, "\"\"")
    val anchor = ILIntroduceVariableHandler.findAnchor(listOf(startElement, endElement))
    if (anchor == null) {
      CommonRefactoringUtil.showErrorHint(project, editor,
          RefactoringBundle.getCannotRefactorMessage(HCLBundle.message("refactoring.introduce.anchor.error")),
          HCLBundle.message("refactoring.introduce.error"), null)
      return
    }
    anchor.parent.addBefore(declaration, anchor) as HCLBlock
  }
}