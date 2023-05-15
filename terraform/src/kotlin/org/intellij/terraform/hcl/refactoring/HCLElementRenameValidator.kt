// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.refactoring

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.StandardPatterns.or
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.HCLParserDefinition
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLStringLiteral


class HCLElementRenameValidator : RenameInputValidator {
  override fun getPattern(): ElementPattern<out PsiElement> {
    return or(
        psiElement(HCLProperty::class.java),
        psiElement(HCLBlock::class.java),
        psiElement(HCLStringLiteral::class.java),
        psiElement(HCLIdentifier::class.java)
    )
  }

  private val lexer = HCLParserDefinition.createLexer()

  override fun isInputValid(name: String, element: PsiElement, context: ProcessingContext): Boolean {
    if (!pattern.accepts(element)) return false
    val identifier = (element as? PsiNameIdentifierOwner)?.nameIdentifier ?: element
    return isInputValid(name, identifier is HCLStringLiteral)
  }

  fun isInputValid(name: String, isStringLiteral: Boolean): Boolean {
    @Suppress("NAME_SHADOWING")
    var name: String = name
    if (isStringLiteral) {
      if (!name.startsWith('\'') && !name.startsWith('\"')) {
        name = "\"" + name
      }
      if (!name.endsWith('\'') && !name.endsWith('\"')) {
        name += "\""
      }
    }
    synchronized(lexer) {
      lexer.start(name)
      return lexer.tokenEnd == name.length && HCLTokenTypes.IDENTIFYING_LITERALS.contains(lexer.tokenType)
    }
  }
}
