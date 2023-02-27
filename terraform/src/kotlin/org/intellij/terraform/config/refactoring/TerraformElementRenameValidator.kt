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
package org.intellij.terraform.config.refactoring

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.StandardPatterns.or
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.config.patterns.TerraformPatterns


class TerraformElementRenameValidator : RenameInputValidator {
  companion object {
    // From https://www.terraform.io/docs/configuration/variables.html
    private val ProhibitedVariableNames = setOf("source", "version", "providers", "count", "for_each", "lifecycle", "depends_on", "locals")

    private fun isInputValid(name: String): Boolean {
      val length = name.length
      if (length == 0) return false
      if (name[0] != '_' && !Character.isUnicodeIdentifierStart(name[0])) return false
      for (i in 1 until length) {
        val c = name[i]
        if (c != '-' && !Character.isUnicodeIdentifierPart(c)) return false
      }
      return true
    }
  }

  override fun getPattern(): ElementPattern<out PsiElement> {
    return or(
        TerraformPatterns.ResourceRootBlock,
        TerraformPatterns.DataSourceRootBlock,
        TerraformPatterns.ModuleRootBlock,
        TerraformPatterns.VariableRootBlock,
        TerraformPatterns.OutputRootBlock
    )
  }

  override fun isInputValid(name: String, element: PsiElement, context: ProcessingContext): Boolean {
    return isInputValid(name, element)
  }

  fun isInputValid(name: String, element: PsiElement): Boolean {
    if (!pattern.accepts(element)) return false
    if (TerraformPatterns.VariableRootBlock.accepts(element)) {
      if (HCLPsiUtil.stripQuotes(name).toLowerCase() in ProhibitedVariableNames) return false
    }

    return isInputValid(name)
  }
}
