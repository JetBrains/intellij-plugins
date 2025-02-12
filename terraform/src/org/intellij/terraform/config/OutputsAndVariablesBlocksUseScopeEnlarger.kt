// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.search.ProjectScopeBuilder
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.UseScopeEnlarger
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.isTerraformCompatiblePsiFile

class OutputsAndVariablesBlocksUseScopeEnlarger : UseScopeEnlarger() {

  override fun getAdditionalUseScope(element: PsiElement): SearchScope? {
    if (element !is HCLElement) return null
    if (!isTerraformCompatiblePsiFile(element.containingFile)) return null
    val block: HCLBlock = element as? HCLBlock ?: return null

    if (!TfPsiPatterns.VariableRootBlock.accepts(block) && !TfPsiPatterns.OutputRootBlock.accepts(block)) return null

    val module = ModuleUtilCore.findModuleForPsiElement(element)
    if (module != null) {
      return module.moduleWithDependentsScope
    }
    return ProjectScopeBuilder.getInstance(element.project).buildProjectScope()
  }
}
