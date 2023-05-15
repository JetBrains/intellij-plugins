// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElementVisitor
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.hil.psi.*
import org.intellij.terraform.hil.psi.impl.getHCLHost

class HILMissingSelfInContextInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = InjectedLanguageManager.getInstance(holder.project).getTopLevelFile(holder.file)
    val ft = file.fileType
    if (ft != TerraformFileType) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

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

      if (getProvisionerResource(host) != null) return
      if (getConnectionResource(host) != null) return

      holder.registerProblem(element, HCLBundle.message("hil.scope.not.available.in.context.inspection.illegal.self.use.message"), ProblemHighlightType.GENERIC_ERROR)
    }
  }

}

