package com.intellij.dts.pp.highlighting

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.pp.lang.psi.PpStatementPsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class PpParserErrorInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : PsiElementVisitor() {
      override fun visitElement(element: PsiElement) {
        if (element is PpStatementPsiElement) {
          registerErrors(element, holder)
        }
      }
    }
  }

  private fun registerErrors(element: PpStatementPsiElement, holder: ProblemsHolder) {
    for (error in element.statement.errors) {
      holder.registerProblem(element, error.range, error.message) // TODO: localize message?
    }
  }
}