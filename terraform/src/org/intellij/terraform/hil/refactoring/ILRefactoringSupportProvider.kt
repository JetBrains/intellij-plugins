// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import com.intellij.refactoring.RefactoringActionHandler
import org.intellij.terraform.hil.psi.ILLiteralExpression
import org.intellij.terraform.hil.psi.ILVariable

class ILRefactoringSupportProvider : RefactoringSupportProvider() {
  override fun isInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
    return element is ILVariable || element is ILLiteralExpression
  }

  override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
    return element is ILVariable || element is ILLiteralExpression
  }

  override fun getIntroduceVariableHandler(): RefactoringActionHandler {
    return ILIntroduceVariableHandler()
  }
}

