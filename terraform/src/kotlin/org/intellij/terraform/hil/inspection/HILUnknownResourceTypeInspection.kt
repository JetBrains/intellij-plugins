/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
