// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.actions.createQuickFixNotInitialized
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.isTfOrTofuPsiFile

class TfNotInitializedInspection : LocalInspectionTool() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTfOrTofuPsiFile(file)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : HCLElementVisitor() {
      override fun visitFile(psiFile: PsiFile) {
        super.visitFile(psiFile)
        val initializedFix = createQuickFixNotInitialized(psiFile)
        if (initializedFix != null) {
          holder.registerProblem(psiFile, HCLBundle.message("not.initialized.inspection.error.message"),
                                 ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                 *arrayOf(initializedFix))
        }
      }
    }
  }

}