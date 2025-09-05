// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

import com.intellij.psi.util.CachedValueProvider
import org.intellij.terraform.config.inspection.TypeSpecificationValidator
import org.intellij.terraform.hcl.psi.HCLExpression

class VariableTypeCachedValueProvider(private val expression: HCLExpression) : CachedValueProvider<HclType?> {
  override fun compute(): CachedValueProvider.Result<HclType?> {
    if (!expression.isValid) {
      return CachedValueProvider.Result(null, expression)
    }
    val type = TypeSpecificationValidator(null, true).getType(expression)
    return CachedValueProvider.Result(type, expression)
  }
}