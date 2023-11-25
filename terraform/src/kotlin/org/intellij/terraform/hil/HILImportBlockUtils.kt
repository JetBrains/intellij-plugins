// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil

import com.intellij.psi.util.parentOfType
import org.intellij.terraform.config.codeinsight.ModelHelper
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

// References inside import blocks have different resolve/completion logic -> we must remember the original PSI location by employing this enumzx
enum class HilContainingBlockType {
  UNSPECIFIED, IMPORT_BLOCK
}

internal fun guessContainingBlockType(expression: BaseExpression): HilContainingBlockType {
  val containingImportBlock = expression.parentOfType<HCLBlock>() ?: return HilContainingBlockType.UNSPECIFIED
  val type = ModelHelper.getBlockType(containingImportBlock)
  return if (type is BlockType && type.literal == "import")
    HilContainingBlockType.IMPORT_BLOCK
  else
    HilContainingBlockType.UNSPECIFIED
}

internal fun getResourceType(resourceDeclaration: HCLBlock): String? {
  return resourceDeclaration.getNameElementUnquoted(1)
}

internal fun getResourceName(resourceDeclaration: HCLBlock): String? {
  return resourceDeclaration.getNameElementUnquoted(2)
}

internal fun getResourceTypeAndName(resourceDeclaration: HCLBlock): String? {
  return listOfNotNull(getResourceType(resourceDeclaration), getResourceName(resourceDeclaration)).joinToString(separator = ".")
}
