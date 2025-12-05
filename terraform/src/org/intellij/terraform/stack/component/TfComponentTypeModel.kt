// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.component

import org.intellij.terraform.config.Constants.HCL_DEPENDS_ON_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_FOR_EACH_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_INPUTS_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_OUTPUT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_SOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TYPE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VARIABLE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VERSION_IDENTIFIER
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.TfTypeModel.Companion.DescriptionProperty
import org.intellij.terraform.config.model.TfTypeModel.Companion.EphemeralProperty
import org.intellij.terraform.config.model.TfTypeModel.Companion.Locals
import org.intellij.terraform.config.model.TfTypeModel.Companion.NullableProperty
import org.intellij.terraform.config.model.TfTypeModel.Companion.RequiredProviders
import org.intellij.terraform.config.model.TfTypeModel.Companion.SensitiveProperty
import org.intellij.terraform.config.model.TfTypeModel.Companion.ValueProperty
import org.intellij.terraform.config.model.TfTypeModel.Companion.VariableDefault
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

internal val TypeProperty: PropertyType = PropertyType(HCL_TYPE_IDENTIFIER, Types.Any, required = true, optional = false)

internal val ComponentVariable: BlockType = BlockType(
  literal = HCL_VARIABLE_IDENTIFIER,
  args = 1,
  properties = listOf(
    TypeProperty,
    VariableDefault,
    DescriptionProperty,
    SensitiveProperty,
    NullableProperty,
    EphemeralProperty
  ).toMap()
)

internal val ComponentOutput: BlockType = BlockType(
  literal = HCL_OUTPUT_IDENTIFIER,
  args = 1,
  properties = listOf(
    TypeProperty,
    ValueProperty,
    DescriptionProperty,
    SensitiveProperty,
    EphemeralProperty
  ).toMap()
)

internal val TfComponentRootBlocks: List<BlockType> = listOf(
  ComponentBlockType,
  RequiredProviders,
  ComponentVariable,
  ComponentOutput,
  Locals
)

internal val TfComponentRootBlocksMap: Map<String, BlockType> = TfComponentRootBlocks.associateBy { it.literal }