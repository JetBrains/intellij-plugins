/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package org.intellij.terraform.config

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.search.ProjectScopeBuilder
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.UseScopeEnlarger
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.config.patterns.TerraformPatterns

class OutputsAndVariablesBlocksUseScopeEnlarger : UseScopeEnlarger() {
  override fun getAdditionalUseScope(element: PsiElement): SearchScope? {
    if (element !is HCLElement) return null
    if (element.containingFile.fileType != TerraformFileType) return null
    val block: HCLBlock = element as? HCLBlock ?: return null

    if (!TerraformPatterns.VariableRootBlock.accepts(block) && !TerraformPatterns.OutputRootBlock.accepts(block)) return null

    val module = ModuleUtilCore.findModuleForPsiElement(element)
    if (module != null) {
      return module.moduleWithDependentsScope
    }
    return ProjectScopeBuilder.getInstance(element.project).buildProjectScope()
  }
}
