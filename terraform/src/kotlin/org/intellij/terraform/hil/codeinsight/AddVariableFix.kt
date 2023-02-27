/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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