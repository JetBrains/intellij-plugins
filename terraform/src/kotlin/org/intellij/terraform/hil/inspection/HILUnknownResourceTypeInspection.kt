// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElementVisitor
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hil.codeinsight.HILCompletionContributor
import org.intellij.terraform.hil.psi.*
import org.intellij.terraform.hil.psi.impl.getHCLHost

class HILUnknownResourceTypeInspection : LocalInspectionTool() {
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
      val host = element.getHCLHost() ?: return
      val parent = element.parent as? ILSelectExpression ?: return
      if (parent.from !== element) return

      val name = element.name ?: return

      if (HILCompletionContributor.SCOPES.contains(name)) return
      if (isExistingResourceType(element, host)) return

      if (DynamicBlockVariableReferenceProvider.getDynamicWithIteratorName(host, name) != null) return
      if (name == "each" &&
          PlatformPatterns.psiElement().inside(
            true,
            PlatformPatterns.or(TerraformPatterns.ResourceRootBlock,
                                TerraformPatterns.DataSourceRootBlock,
                                TerraformPatterns.ModuleRootBlock)
          ).accepts(host)) return

      if (element.references.any { it is ForVariableDirectReference && it.resolve() != null }) return

      holder.registerProblem(element, HCLBundle.message("hil.unknown.resource.type.inspection.unknown.resource.type.error.message"),
                             ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
    }
  }

}


fun isExistingResourceType(element: ILVariable, host: HCLElement): Boolean {
  val name = element.name
  val module = host.getTerraformModule()
  return module.findResources(name, null).isNotEmpty()
}
