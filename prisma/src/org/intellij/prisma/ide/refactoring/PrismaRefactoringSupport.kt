package org.intellij.prisma.ide.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement

class PrismaRefactoringSupport : RefactoringSupportProvider() {
  override fun isInplaceRenameAvailable(element: PsiElement, context: PsiElement?) = true

  override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?) = true
}