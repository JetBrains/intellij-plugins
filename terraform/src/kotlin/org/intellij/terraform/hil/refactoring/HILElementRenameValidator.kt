// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.refactoring

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.StandardPatterns.or
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hil.HILElementTypes
import org.intellij.terraform.hil.psi.HILLexer
import org.intellij.terraform.hil.psi.ILProperty
import org.intellij.terraform.hil.psi.ILVariable


class HILElementRenameValidator : RenameInputValidator {
  override fun getPattern(): ElementPattern<out PsiElement> {
    return or(
        psiElement(ILVariable::class.java),
        psiElement(ILProperty::class.java)
    )
  }

  private val lexer = HILLexer()

  override fun isInputValid(name: String, element: PsiElement, context: ProcessingContext): Boolean {
    if (!pattern.accepts(element)) return false
    synchronized(lexer) {
      lexer.start(name)
      return lexer.tokenEnd == name.length && HILElementTypes.ID == lexer.tokenType
    }
  }
}
