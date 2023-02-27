/*
 * Copyright 2000-2019 JetBrains s.r.o.
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