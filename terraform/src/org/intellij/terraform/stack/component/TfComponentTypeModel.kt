// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.component

import org.intellij.terraform.config.Constants.HCL_DEPENDS_ON_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_FOR_EACH_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_INPUTS_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_SOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VERSION_IDENTIFIER
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.config.model.toMap

internal val ComponentBlockType: BlockType = BlockType(
  literal = "component",
  args = 1,
  properties = listOf(
    PropertyType(HCL_SOURCE_IDENTIFIER, Types.String, required = true, optional = false),
    PropertyType(HCL_VERSION_IDENTIFIER, Types.String),
    PropertyType(HCL_INPUTS_IDENTIFIER, Types.Object, required = true, optional = false),
    PropertyType("providers", Types.Object, required = true, optional = false),
    PropertyType(HCL_DEPENDS_ON_IDENTIFIER, Types.Array),
    PropertyType(HCL_FOR_EACH_IDENTIFIER, Types.Any)
  ).toMap()
)

internal val TfComponentRootBlocks: List<BlockType> = listOf(ComponentBlockType)