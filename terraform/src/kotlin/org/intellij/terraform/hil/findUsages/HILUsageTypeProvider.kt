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
