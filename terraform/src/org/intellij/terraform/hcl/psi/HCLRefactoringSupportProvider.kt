// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.RefactoringActionHandler
import org.intellij.terraform.config.refactoring.TerraformIntroduceVariableHandler

class HCLRefactoringSupportProvider : RefactoringSupportProvider() {
  override fun isAvailable(context: PsiElement): Boolean {
    return context is HCLElement && context is PsiNamedElement
  }

  // Inplace refactoring supported only if element#getUseScope is instance of LocalSearchScope
  override fun isInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
    return element is HCLElement && element is PsiNamedElement
  }

  override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
    return element is HCLElement && element is PsiNamedElement
  }

  override fun getIntroduceVariableHandler(): RefactoringActionHandler {
    return TerraformIntroduceVariableHandler()
  }

  override fun getIntroduceVariableHandler(element: PsiElement?): RefactoringActionHandler {
    return TerraformIntroduceVariableHandler()
  }
}