// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.navigation.HCLQualifiedNameProvider
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor

// TODO: Support overrides in separate files
class TfDuplicatedBlockPropertyInspection : TfDuplicatedInspectionBase() {

  override fun createVisitor(holder: ProblemsHolder): PsiElementVisitor {
    return MyEV(holder)
  }

  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      // TODO: Check whether it's correct to check FQN
      HCLQualifiedNameProvider.getFQN(block) ?: return
      // TODO: Support sub-blocks (based on model)
      val properties = block.`object`?.propertyList ?: return
      val model = TfModelHelper.getBlockProperties(block)
      val groupedDuplicates = properties.groupBy { it.name }
        .filterValues { it.size >= 2 }
        .filterKeys { model[it] is PropertyType }
      for ((name, props) in groupedDuplicates) {
        for (prop in props) {
          holder.registerProblem(prop.nameElement,
                                 HCLBundle.message("duplicated.block.property.inspection.duplicate.property.error.message", name),
                                 ProblemHighlightType.GENERIC_ERROR, *getDefaultFixes(prop, props))
        }
      }
    }
  }
}