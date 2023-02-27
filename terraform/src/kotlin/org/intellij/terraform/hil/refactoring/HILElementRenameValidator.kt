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
