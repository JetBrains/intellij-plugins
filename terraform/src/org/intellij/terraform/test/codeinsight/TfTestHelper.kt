// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.test.codeinsight

import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.test.isTfMockPsiFile
import org.intellij.terraform.test.model.TfMockRootBlocksMap
import org.intellij.terraform.test.model.TfTestRootBlocksMap
import org.intellij.terraform.test.patterns.TfTestPsiPatterns

internal object TfTestHelper {
  fun getBlockProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(0) ?: return emptyMap()

    when {
      TfTestPsiPatterns.TfVariablesBlock.accepts(block) -> return getVariablesOfTfModule(block)
      block.parent !is PsiFile -> return TfModelHelper.traverseParentBlockProperties(block, type)
    }

    return when (type) {
      HCL_PROVIDER_IDENTIFIER -> TfModelHelper.getProviderProperties(block)
      else -> getTfTestRootBlock(block, type)?.properties.orEmpty()
    }
  }

  private fun getTfTestRootBlock(block: HCLBlock, type: String): BlockType? {
    val file = block.containingFile.originalFile
    val rootBlocks = if (isTfMockPsiFile(file)) TfMockRootBlocksMap else TfTestRootBlocksMap
    return rootBlocks[type]
  }

  private fun getVariablesOfTfModule(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val file = block.containingFile.originalFile
    if (isTfMockPsiFile(file)) return emptyMap()

    val moduleBlock = findDeclaredModuleInRunBlock(block)
    val computedModule = if (moduleBlock != null) Module.getAsModuleBlock(moduleBlock) else block.getTerraformModule()

    return computedModule?.getAllVariables()
      .orEmpty()
      .distinctBy { it.name }
      .associate { variable ->
        variable.name to PropertyType(variable.name, variable.getType() ?: Types.Any)
      }
  }

  private fun findDeclaredModuleInRunBlock(variablesBlock: HCLBlock): HCLBlock? {
    val runBlock = variablesBlock.parentOfType<HCLBlock>() ?: return null
    if (!TfTestPsiPatterns.TfRunBlock.accepts(runBlock))
      return null

    return runBlock.`object`?.blockList?.firstOrNull { TfTestPsiPatterns.TfModuleBlock.accepts(it) }
  }
}