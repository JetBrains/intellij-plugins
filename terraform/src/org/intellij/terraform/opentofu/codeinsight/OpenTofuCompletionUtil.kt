// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu.codeinsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentsOfType
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.opentofu.patterns.OpenTofuPatterns.EncryptionBlock
import kotlin.collections.filter

internal fun findEncryptionBlocksIdsByType(element: PsiElement, blockType: String?, blockPattern: PsiElementPattern.Capture<HCLBlock>): Sequence<LookupElement> {
  blockType ?: return emptySequence()
  val keyProviderNames = findEncryptionBlockElements(element, blockPattern)
                           .filter { block -> block.getNameElementUnquoted(1)?.contentEquals(blockType) == true }.orEmpty()
  return keyProviderNames.map {
    LookupElementBuilder.create(it.getNameElementUnquoted(2)!!)
      .withTypeText(it.getNameElementUnquoted(1))
      .withIcon(TerraformIcons.CollectionKey)
  }.asSequence()
}

internal fun findEncryptionBlockElements(element: PsiElement, template: PsiElementPattern.Capture<HCLBlock>): List<HCLBlock> {
  val cachedResult = CachedValuesManager.getCachedValue(element) {
    val relevantBlocks = element.parentsOfType<HCLBlock>(true)
      .firstOrNull { block -> EncryptionBlock.accepts(block) }
      ?.`object`
      ?.childrenOfType<HCLBlock>().orEmpty()
    CachedValueProvider.Result.create(relevantBlocks, PsiModificationTracker.MODIFICATION_COUNT)
  }
  return cachedResult.filter { block -> template.accepts(block) }
}