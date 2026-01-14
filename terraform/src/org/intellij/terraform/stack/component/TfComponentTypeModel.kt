// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.component

import org.intellij.terraform.config.Constants.HCL_CONFIG_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_DEPENDS_ON_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_FOR_EACH_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_INPUTS_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_OUTPUT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_REMOVED_BLOCK_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_SOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TYPE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VARIABLE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VERSION_IDENTIFIER
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.TfTypeModel.Companion.DescriptionProperty
import org.intellij.terraform.config.model.TfTypeModel.Companion.EphemeralProperty
import org.intellij.terraform.config.model.TfTypeModel.Companion.FromProperty
import org.intellij.terraform.config.model.TfTypeModel.Companion.Locals
import org.intellij.terraform.config.model.TfTypeModel.Companion.NullableProperty
import org.intellij.terraform.config.model.TfTypeModel.Companion.RequiredProviders
import org.intellij.terraform.config.model.TfTypeModel.Companion.SensitiveProperty
import org.intellij.terraform.config.model.TfTypeModel.Companion.ValueProperty
import org.intellij.terraform.config.model.TfTypeModel.Companion.VariableDefault
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.config.model.toMap
import org.intellij.terraform.terragrunt.TERRAGRUNT_STACK

private val SourceProperty: PropertyType = PropertyType(HCL_SOURCE_IDENTIFIER, Types.String, required = true)
private val ProvidersProperty: PropertyType = PropertyType("providers", Types.Object, required = true)
private val ForEachProperty: PropertyType = PropertyType(HCL_FOR_EACH_IDENTIFIER, Types.Any)
private val TypeProperty: PropertyType = PropertyType(HCL_TYPE_IDENTIFIER, Types.Any, required = true)

internal val InputsProperty = PropertyType(HCL_INPUTS_IDENTIFIER, Types.Object, required = true)

internal val ComponentBlockType: BlockType = BlockType(
  "component", 1,
  properties = listOf(
    SourceProperty,
    createVersionProperty(),
    InputsProperty,
    ProvidersProperty,
    PropertyType(HCL_DEPENDS_ON_IDENTIFIER, Types.Array),
    ForEachProperty
  ).toMap()
)

internal val TfStackBlock: BlockType = BlockType(
  TERRAGRUNT_STACK, 1,
  properties = listOf(
    SourceProperty,
    createVersionProperty(isRequired = true),
    InputsProperty
  ).toMap()
)

internal val ComponentProvider: BlockType = BlockType(
  HCL_PROVIDER_IDENTIFIER, 2,
  properties = listOf(
    BlockType(HCL_CONFIG_IDENTIFIER),
    ForEachProperty
  ).toMap()
)

internal val ComponentVariable: BlockType = BlockType(
  HCL_VARIABLE_IDENTIFIER, 1,
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
  HCL_OUTPUT_IDENTIFIER, 1,
  properties = listOf(
    TypeProperty,
    ValueProperty,
    DescriptionProperty,
    SensitiveProperty,
    EphemeralProperty
  ).toMap()
)

internal val RemovedComponent: BlockType = BlockType(
  HCL_REMOVED_BLOCK_IDENTIFIER,
  properties = listOf(
    SourceProperty,
    FromProperty,
    ProvidersProperty,
    ForEachProperty
  ).toMap()
)

internal val TfComponentRootBlocks: List<BlockType> = listOf(
  ComponentBlockType,
  TfStackBlock,
  RequiredProviders,
  ComponentProvider,
  ComponentVariable,
  ComponentOutput,
  RemovedComponent,
  Locals
)

internal val TfComponentRootBlocksMap: Map<String, BlockType> = TfComponentRootBlocks.associateBy { it.literal }

internal fun createVersionProperty(isRequired: Boolean = false) = PropertyType(HCL_VERSION_IDENTIFIER, Types.String, required = isRequired)