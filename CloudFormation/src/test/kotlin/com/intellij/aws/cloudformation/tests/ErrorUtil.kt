package com.intellij.aws.cloudformation.tests

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiRecursiveElementVisitor

class ErrorUtil private constructor() {
  private class ErrorElementVisitor : PsiRecursiveElementVisitor() {
    private var containsErrorElement = false

    override fun visitErrorElement(element: PsiErrorElement) {
      containsErrorElement = true
    }

    fun containsErrorElement(): Boolean {
      return containsErrorElement
    }
  }

  companion object {

    fun containsError(element: PsiElement): Boolean {
      val visitor = ErrorElementVisitor()
      element.accept(visitor)
      return visitor.containsErrorElement()
    }
  }
}