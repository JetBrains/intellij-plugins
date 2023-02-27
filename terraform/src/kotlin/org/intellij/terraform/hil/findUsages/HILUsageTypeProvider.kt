// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.findUsages

import com.intellij.psi.PsiElement
import com.intellij.usages.impl.rules.UsageType
import com.intellij.usages.impl.rules.UsageTypeProvider
import org.intellij.terraform.hil.psi.ILExpression
import org.intellij.terraform.hil.psi.ILLiteralExpression
import org.intellij.terraform.hil.psi.ILProperty
import org.intellij.terraform.hil.psi.ILSelectExpression

class HILUsageTypeProvider : UsageTypeProvider {

  override fun getUsageType(element: PsiElement): UsageType? {
    if (element is ILExpression) {
      val parent = element.parent
      if (parent is ILSelectExpression) {
        if (parent.field === element) {
          return UsageType.READ
        }
      }
      if (element is ILLiteralExpression) {
        return UsageType.LITERAL_USAGE
      }
    }
    if (element is ILProperty) {
      return UsageType.WRITE
    }
    return null
  }
}
