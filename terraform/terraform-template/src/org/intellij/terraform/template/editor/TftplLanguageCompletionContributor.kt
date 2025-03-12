// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.editor

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.hil.HILElementTypes

internal class TftplLanguageCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC, hilEmptyControlStructurePattern, HilTemplateAvailableSyntaxCompletionProvider())
  }
}

private val hilEmptyControlStructurePattern: ElementPattern<PsiElement> =
  createPattern { element ->
    element is LeafPsiElement
    && getTemplateFileViewProvider(element) != null
    && PsiTreeUtil.prevCodeLeaf(element)?.elementType == HILElementTypes.TEMPLATE_START
  }

private class HilTemplateAvailableSyntaxCompletionProvider : CompletionProvider<CompletionParameters>() {
  private val availableControlStructures = setOf("if", "else", "for", "endif", "endfor")

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    result.addAllElements(availableControlStructures.map(::createLookup))
  }

  private fun createLookup(lookupText: String): LookupElement {
    return LookupElementBuilder.create(lookupText)
      .withIcon(TerraformIcons.Terraform)
  }
}