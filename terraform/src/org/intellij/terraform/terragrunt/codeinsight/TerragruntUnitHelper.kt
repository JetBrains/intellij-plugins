// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.codeinsight

import com.intellij.psi.PsiFile
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.terragrunt.isTerragruntStack
import org.intellij.terraform.terragrunt.model.StackRootBlocksMap
import org.intellij.terraform.terragrunt.model.TerragruntRootBlocksMap

internal object TerragruntUnitHelper {
  fun getBlockProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(0) ?: return emptyMap()
    // For now, only root blocks are supported
    if (block.parent !is PsiFile) return TfModelHelper.traverseParentBlockProperties(block, type)

    val file = block.containingFile.originalFile
    val rootBlocks = if (isTerragruntStack(file)) StackRootBlocksMap else TerragruntRootBlocksMap

    return rootBlocks[type]?.properties.orEmpty()
  }

  fun collectMatchingBlocks(block: HCLBlock, keyword: String, firstArgument: String): List<HCLBlock> {
    val found = mutableListOf<HCLBlock>()
    val file = block.containingFile.originalFile

    file.acceptChildren(object : HCLElementVisitor() {
      override fun visitBlock(block: HCLBlock) {
        if (keyword != block.getNameElementUnquoted(0)) return

        val label = block.getNameElementUnquoted(1) ?: return
        if (label != firstArgument) return

        found.add(block)
      }
    })

    return found
  }
}