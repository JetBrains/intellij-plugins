// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.deployment

import com.intellij.psi.PsiFile
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

internal object TfDeployHelper {
  fun getBlockProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(0) ?: return emptyMap()
    if (block.parent !is PsiFile)
      return TfModelHelper.traverseParentBlockProperties(block, type)

    return TfDeployRootBlocksMap[type]?.properties.orEmpty()
  }
}
