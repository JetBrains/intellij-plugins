// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.StandardPatterns.or
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import java.util.Locale

internal class TfElementRenameValidator : RenameInputValidator {
  private val reservedKeywords = setOf("source", "version", "providers", "count", "for_each", "lifecycle", "depends_on", "locals")

  override fun getPattern(): ElementPattern<out PsiElement> {
    return or(
      TfPsiPatterns.ResourceRootBlock,
      TfPsiPatterns.DataSourceRootBlock,
      TfPsiPatterns.ModuleRootBlock,
      TfPsiPatterns.VariableRootBlock,
      TfPsiPatterns.OutputRootBlock
    )
  }

  override fun isInputValid(name: String, element: PsiElement, context: ProcessingContext): Boolean {
    return isInputValid(name, element)
  }

  fun isInputValid(name: String, element: PsiElement): Boolean {
    if (!pattern.accepts(element)) return false
    if (TfPsiPatterns.VariableRootBlock.accepts(element)) {
      if (HCLPsiUtil.stripQuotes(name).lowercase(Locale.getDefault()) in reservedKeywords) return false
    }

    return isValidHclIdentifier(name)
  }
}

internal fun isValidHclIdentifier(name: String): Boolean {
  if (name.isEmpty()) return false
  if (!isHclIdentifierStart(name.first())) return false

  for (i in 1 until name.length) {
    if (!isHclIdentifierPart(name[i])) return false
  }
  return true
}

private fun isHclIdentifierStart(c: Char): Boolean =
  c == '_' || Character.isUnicodeIdentifierStart(c)

private fun isHclIdentifierPart(c: Char): Boolean =
  c == '-' || Character.isUnicodeIdentifierPart(c)
