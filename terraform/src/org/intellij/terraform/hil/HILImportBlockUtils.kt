// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil

import com.intellij.psi.util.parentOfType
import org.intellij.terraform.config.Constants.HCL_IMPORT_IDENTIFIER
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

// References inside import blocks have different resolve/completion logic -> we must remember the original PSI location by employing this enumzx
enum class HilContainingBlockType {
  UNSPECIFIED, IMPORT_OR_MOVED_BLOCK
}

internal fun guessContainingBlockType(expression: BaseExpression): HilContainingBlockType {
  val containingImportBlock = expression.parentOfType<HCLBlock>() ?: return HilContainingBlockType.UNSPECIFIED
  val type = TfModelHelper.getBlockType(containingImportBlock)
  return if (type is BlockType && (type.literal == HCL_IMPORT_IDENTIFIER || type.literal == "moved"))
    HilContainingBlockType.IMPORT_OR_MOVED_BLOCK
  else
    HilContainingBlockType.UNSPECIFIED
}

internal fun getResourceType(resourceDeclaration: HCLBlock): String? {
  return resourceDeclaration.getNameElementUnquoted(1)
}

internal fun getResourceName(resourceDeclaration: HCLBlock): String? {
  return resourceDeclaration.getNameElementUnquoted(2)
}

internal fun getResourceTypeAndName(resourceDeclaration: HCLBlock): String {
  return listOfNotNull(getResourceType(resourceDeclaration), getResourceName(resourceDeclaration)).joinToString(separator = ".")
}
