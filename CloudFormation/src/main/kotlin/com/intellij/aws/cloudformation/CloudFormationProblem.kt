package com.intellij.aws.cloudformation

import com.intellij.psi.PsiElement

class CloudFormationProblem(val element: PsiElement, val description: String) {
  override fun toString(): String {
    return "Problem(line=${CloudFormationPsiUtils.getLineNumber(element)}, description='$description')"
  }
}