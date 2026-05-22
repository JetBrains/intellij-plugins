// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.test.codeinsight

import com.intellij.psi.PsiFile
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.test.isTfMockPsiFile
import org.intellij.terraform.test.model.TfMockRootBlocksMap
import org.intellij.terraform.test.model.TfTestRootBlocksMap

internal object TfTestHelper {
  fun getBlockProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(0) ?: return emptyMap()
    if (block.parent !is PsiFile) {
      return TfModelHelper.traverseParentBlockProperties(block, type)
    }
    if (type == Constants.HCL_PROVIDER_IDENTIFIER) {
      return TfModelHelper.getProviderProperties(block)
    }

    val file = block.containingFile.originalFile
    val rootBlocks = if (isTfMockPsiFile(file)) TfMockRootBlocksMap else TfTestRootBlocksMap
    return rootBlocks[type]?.properties.orEmpty()
  }
}