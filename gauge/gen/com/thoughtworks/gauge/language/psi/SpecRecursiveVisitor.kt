package com.thoughtworks.gauge.language.psi;

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement

public open class SpecRecursiveVisitor : SpecVisitor() {
  override fun visitElement(element: PsiElement) {
    ProgressManager.checkCanceled()
    element.acceptChildren(this)
  }
}
