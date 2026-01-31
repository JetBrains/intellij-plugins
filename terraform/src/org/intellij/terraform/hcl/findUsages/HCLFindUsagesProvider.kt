// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.findUsages

import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.createHclLexer
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.jetbrains.annotations.Nls

open class HCLFindUsagesProvider : FindUsagesProvider {
  override fun getWordsScanner(): WordsScanner? {
    return HCLWordsScanner(createHclLexer())
  }

  override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
    if (psiElement !is PsiNamedElement || psiElement !is HCLElement) return false

    if (psiElement is HCLIdentifier) {
      val isDynamicIterator = HCLPsiUtil.isPropertyValue(psiElement) && TfPsiPatterns.DynamicBlockIterator.accepts(psiElement.parent)
      val isForVariable = TfPsiPatterns.ForVariable.accepts(psiElement)

      if (isDynamicIterator || isForVariable) return true
    }

    return !TfPsiPatterns.LocalsRootBlock.accepts(psiElement) && !TfPsiPatterns.TerraformRootBlock.accepts(psiElement)
  }

  override fun getHelpId(psiElement: PsiElement): String? {
    return null
  }

  override fun getType(element: PsiElement): String {
    if (!TfPsiPatterns.TerraformFile.accepts(element.containingFile)) {
      return getDefaultType(element)
    }

    return when (element) {
      is HCLBlock -> getBlockType(element)
      is HCLProperty -> getPropertyType(element)
      is HCLIdentifier -> getIdentifierType(element)
      else -> getDefaultType(element)
    }
  }

  private fun getBlockType(block: HCLBlock): @NlsSafe String {
    val type = block.getNameElementUnquoted(0)

    return when {
      TfPsiPatterns.RootBlock.accepts(block) -> getRootBlockType(type)
      TfPsiPatterns.Backend.accepts(block) -> HCLBundle.message("HCLFindUsagesProvider.type.backend.configuration")
      else -> type.toString()
    }
  }

  private fun getRootBlockType(type: String?): @NlsSafe String = when (type) {
    "module" -> HCLBundle.message("HCLFindUsagesProvider.type.module")
    "variable" -> HCLBundle.message("HCLFindUsagesProvider.type.variable")
    "output" -> HCLBundle.message("HCLFindUsagesProvider.type.output.value")
    "provider" -> HCLBundle.message("HCLFindUsagesProvider.type.provider")
    "resource" -> HCLBundle.message("HCLFindUsagesProvider.type.resource")
    "data" -> HCLBundle.message("HCLFindUsagesProvider.type.data.source")
    "terraform" -> HCLBundle.message("HCLFindUsagesProvider.type.terraform.configuration")
    "locals" -> HCLBundle.message("HCLFindUsagesProvider.type.local.values")
    else -> type.toString()
  }

  private fun getPropertyType(property: PsiElement): @Nls String = when {
    TfPsiPatterns.LocalsVariable.accepts(property) -> HCLBundle.message("HCLFindUsagesProvider.type.local.value")
    else -> HCLBundle.message("HCLFindUsagesProvider.type.property")
  }

  private fun getIdentifierType(identifier: HCLIdentifier): @Nls String = when {
    HCLPsiUtil.isPropertyValue(identifier) && TfPsiPatterns.DynamicBlockIterator.accepts(identifier.parent) ->
      HCLBundle.message("HCLFindUsagesProvider.type.dynamic.iterator")
    TfPsiPatterns.ForVariable.accepts(identifier) ->
      HCLBundle.message("HCLFindUsagesProvider.type.for.loop.variable")
    identifier.parent is HCLProperty -> getPropertyType(identifier.parent)
    else -> getDefaultType(identifier)
  }

  private fun getDefaultType(element: PsiElement): @Nls String = when (element) {
    is HCLBlock -> HCLBundle.message("HCLFindUsagesProvider.type.named.block", element.getNameElementUnquoted(0))
    is HCLProperty -> HCLBundle.message("HCLFindUsagesProvider.type.property")
    is PsiNamedElement -> HCLBundle.message("HCLFindUsagesProvider.type.untyped.named.element", element.javaClass.name)
    else -> HCLBundle.message("HCLFindUsagesProvider.type.untyped.non.psi.named.element", element.node.elementType)
  }

  override fun getDescriptiveName(element: PsiElement): String {
    val name = if (element is PsiNamedElement) element.name else null
    return name ?: "<Not An PsiNamedElement ${element.node.elementType}>"
  }

  override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
    if (useFullName && element is HCLBlock) {
      return element.fullName
    }
    return getDescriptiveName(element)
  }
}
