// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.component

import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

internal object TfComponentHelper {
  fun getBlockProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(0) ?: return emptyMap()
    when {
      TfComponentPsiPatterns.ConfigOfProvider.accepts(block) -> return getProviderProperties(block)
      block.parent !is PsiFile -> return TfModelHelper.traverseParentBlockProperties(block, type)
    }

    return TfComponentRootBlocksMap[type]?.properties.orEmpty()
  }

  private fun getProviderProperties(configBlock: HCLBlock): Map<String, PropertyOrBlockType> {
    val providerBlock = configBlock.parentOfType<HCLBlock>() ?: return emptyMap()

    val tfModel = TypeModelProvider.getModel(providerBlock)
    val type = providerBlock.getNameElementUnquoted(1)
    val providerType = if (!type.isNullOrBlank()) tfModel.getProviderType(type, providerBlock) else null
    return providerType?.properties ?: emptyMap()
  }
}