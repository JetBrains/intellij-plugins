// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.getConnectionOfResource
import org.intellij.terraform.hcl.psi.getProvisionerOfResource
import org.intellij.terraform.hil.psi.ILElementVisitor
import org.intellij.terraform.hil.psi.ILSelectExpression
import org.intellij.terraform.hil.psi.ILVariable
import org.intellij.terraform.hil.psi.impl.getHCLHost
import org.intellij.terraform.isTerraformCompatiblePsiFile

class HILMissingSelfInContextInspection : LocalInspectionTool() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    val topLevelFile = InjectedLanguageManager.getInstance(file.project).getTopLevelFile(file)
    return isTerraformCompatiblePsiFile(topLevelFile)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return MyEV(holder)
  }

  inner class MyEV(val holder: ProblemsHolder) : ILElementVisitor() {
    override fun visitILVariable(element: ILVariable) {
      ProgressIndicatorProvider.checkCanceled()
      val name = element.name
      if ("self" != name) return

      val host = element.getHCLHost() ?: return
      val parent = element.parent as? ILSelectExpression ?: return
      if (parent.from !== element) return

      if (getProvisionerOfResource(host) != null) return
      if (getConnectionOfResource(host) != null) return

      holder.registerProblem(element, HCLBundle.message("hil.scope.not.available.in.context.inspection.illegal.self.use.message"), ProblemHighlightType.GENERIC_ERROR)
    }
  }

}

