/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import com.intellij.lang.LanguageNamesValidation
import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.hil.HILLanguage

open class IntroduceValidator {
  private val myNamesValidator: NamesValidator = LanguageNamesValidation.INSTANCE.forLanguage(HILLanguage)

  fun isNameValid(name: String?, project: Project): Boolean {
    return name != null &&
           (myNamesValidator.isIdentifier(name, project) &&
            !myNamesValidator.isKeyword(name, project))
  }

  fun checkPossibleName(name: String, expression: HCLElement): Boolean {
    return check(name, expression) == null
  }

  fun check(name: String, psiElement: HCLElement): String? {
    if (isDefinedInScope(name, psiElement)) {
      return HCLBundle.message("refactoring.introduce.variable.scope.error")
    }
    return null
  }

  companion object {

    fun isDefinedInScope(name: String, psiElement: HCLElement): Boolean {
      @Suppress("DEPRECATION")
      val variable = psiElement.getTerraformModule().findVariable(name)
      return variable != null
    }
  }
}