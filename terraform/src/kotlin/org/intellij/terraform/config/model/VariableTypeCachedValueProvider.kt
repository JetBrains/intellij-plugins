// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

import com.intellij.psi.util.CachedValueProvider
import org.intellij.terraform.hcl.psi.HCLExpression
import org.intellij.terraform.config.inspection.TypeSpecificationValidator

class VariableTypeCachedValueProvider(private val expression: HCLExpression) : CachedValueProvider<Type?> {
  override fun compute(): CachedValueProvider.Result<Type?> {
    if (!expression.isValid) {
      return CachedValueProvider.Result(null, expression)
    }
    val type = TypeSpecificationValidator(null, true).getType(expression)
    return CachedValueProvider.Result(type, expression)
  }
}