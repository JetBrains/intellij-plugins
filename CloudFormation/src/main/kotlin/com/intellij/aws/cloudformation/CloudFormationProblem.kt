package com.intellij.aws.cloudformation

import com.intellij.psi.PsiElement
import org.jetbrains.annotations.Nls

class CloudFormationProblem(val element: PsiElement, @Nls val description: String) {
  override fun toString(): String {
    return "Problem(line=${CloudFormationPsiUtils.getLineNumber(element)}, description='$description')"
  }
}