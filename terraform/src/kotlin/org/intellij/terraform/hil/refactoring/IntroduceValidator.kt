// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.refactoring

import com.intellij.lang.LanguageNamesValidation
import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.hil.HILLanguage
import org.intellij.terraform.hil.psi.ILExpression
import org.intellij.terraform.hil.psi.impl.getHCLHost

open class IntroduceValidator {
  private val myNamesValidator: NamesValidator = LanguageNamesValidation.INSTANCE.forLanguage(HILLanguage)

  fun isNameValid(name: String?, project: Project): Boolean {
    return name != null &&
           (myNamesValidator.isIdentifier(name, project) &&
            !myNamesValidator.isKeyword(name, project))
  }

  fun checkPossibleName(name: String, expression: ILExpression): Boolean {
    return check(name, expression) == null
  }

  fun check(name: String, psiElement: ILExpression): String? {
    if (isDefinedInScope(name, psiElement)) {
      return HCLBundle.message("refactoring.introduce.variable.scope.error")
    }
    return null
  }

  companion object {

    fun isDefinedInScope(name: String, psiElement: ILExpression): Boolean {
      val module = psiElement.getHCLHost()?.getTerraformModule() ?: return true // TODO: Check whether return needed here
      @Suppress("DEPRECATION")
      val variable = module.findVariable(name)
      return variable != null
    }
  }
}