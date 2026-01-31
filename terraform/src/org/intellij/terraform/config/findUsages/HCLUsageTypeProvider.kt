// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.findUsages

import com.intellij.psi.PsiElement
import com.intellij.usages.impl.rules.UsageType
import com.intellij.usages.impl.rules.UsageTypeProvider
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLExpression
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLSelectExpression

private class HCLUsageTypeProvider : UsageTypeProvider {

  override fun getUsageType(element: PsiElement): UsageType? {
    if (element !is HCLElement) return null
    if (element is HCLProperty || element is HCLBlock){
      return UsageType.WRITE
    }
    if (element is HCLExpression) {
      val parent = element.parent
      if (parent is HCLSelectExpression) {
        if (parent.field === element) {
          return UsageType.READ
        }
      }
    }
    return null
  }
}
