// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.editor

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import org.intellij.terraform.template.model.TftplVariable
import org.intellij.terraform.template.model.collectAvailableVariables
import org.intellij.terraform.withGuaranteedProgressIndicator

class TftplVariableGoToDeclarationHandler : GotoDeclarationHandler {
  override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor?): Array<PsiElement> {
    val effectiveVariableToSearch = when {
      sourceElement == null -> return emptyArray()
      isTftplVariable(sourceElement) -> sourceElement
      isInjectedVariable(sourceElement) -> translateToTemplateLanguageElement(sourceElement)
      else -> return emptyArray()
    }

    return tryResolveVariable(effectiveVariableToSearch).toList().toTypedArray()
  }

  private fun isTftplVariable(psiElement: PsiElement?): Boolean {
    return psiElement != null && hilVariablePattern.accepts(psiElement)
  }

  private fun isInjectedVariable(psiElement: PsiElement?): Boolean {
    return psiElement != null && injectedVariablePattern.accepts(psiElement)
  }

  private fun tryResolveVariable(sourceElement: PsiElement?): Sequence<PsiElement> {
    val searchedVariableName = sourceElement?.text?.takeIf(String::isNotBlank) ?: return emptySequence()
    return withGuaranteedProgressIndicator {
      collectAvailableVariables(sourceElement)
        .filter { variable -> variable.name == searchedVariableName }
        .map(TftplVariable::navigationElement)
    }
  }
}

private val injectedVariablePattern = createHilVariablePattern(::isInjectedHil)