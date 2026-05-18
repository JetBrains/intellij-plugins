// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.test

import org.intellij.terraform.config.Constants.HCL_ASSERT_BLOCK_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_CONDITION_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_ERROR_MESSAGE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_MODULE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDERS_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_SOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VERSION_IDENTIFIER
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.SimpleValueHint
import org.intellij.terraform.config.model.TfTypeModel
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.config.model.toMap

private val ParallelProperty: PropertyType = PropertyType(HCL_PARALLEL_IDENTIFIER, Types.Boolean)

private val TfTestBlock: BlockType = BlockType(HCL_TEST_IDENTIFIER, properties = listOf(ParallelProperty).toMap())

private val TfTestVariablesBlock: BlockType = BlockType(HCL_VARIABLES_IDENTIFIER)

private val TfTestModuleBlock: BlockType = BlockType(
  HCL_MODULE_IDENTIFIER,
  properties = listOf(
    PropertyType(HCL_SOURCE_IDENTIFIER, Types.String, required = true),
    PropertyType(HCL_VERSION_IDENTIFIER, Types.String)
  ).toMap()
)

private val TfTestPlanOptionsBlock: BlockType = BlockType(
  "plan_options",
  properties = listOf(
    PropertyType("mode", Types.Identifier, hint = SimpleValueHint("normal", "refresh-only")),
    PropertyType("refresh", Types.Boolean),
    PropertyType("replace", Types.Array),
    PropertyType(HCL_TARGET_IDENTIFIER, Types.Array)
  ).toMap()
)

private val TfTestAssertBlock: BlockType = BlockType(
  HCL_ASSERT_BLOCK_IDENTIFIER,
  properties = listOf(
    PropertyType(HCL_CONDITION_IDENTIFIER, Types.Boolean, required = true),
    PropertyType(HCL_ERROR_MESSAGE_IDENTIFIER, Types.String, required = true)
  ).toMap()
)

private val ApplyPlanHints = SimpleValueHint("apply", "plan")

private val TfTestRunBlock: BlockType = BlockType(
  HCL_RUN_IDENTIFIER,
  1,
  properties = listOf(
    PropertyType("command", Types.Identifier, hint = ApplyPlanHints),
    TfTestPlanOptionsBlock,
    TfTestVariablesBlock,
    TfTestModuleBlock,
    PropertyType(HCL_PROVIDERS_IDENTIFIER, Types.Object),
    TfTestAssertBlock,
    PropertyType("expect_failures", Types.Array),
    PropertyType("state_key", Types.String),
    ParallelProperty,
  ).toMap()
)

private val TargetProperty: PropertyType = PropertyType(HCL_TARGET_IDENTIFIER, Types.Identifier, required = true)
private val ValuesProperty: PropertyType = PropertyType("values", Types.Object)

private val OverrideDuringProperty: PropertyType = PropertyType(
  "override_during",
  Types.Identifier,
  hint = SimpleValueHint("apply", "plan")
)

private val DefaultsProperty: PropertyType = PropertyType("defaults", Types.Object)

// Override blocks
private val TfOverrideResourceBlock: BlockType = BlockType(
  HCL_OVERRIDE_RESOURCE_IDENTIFIER,
  properties = listOf(TargetProperty, ValuesProperty, OverrideDuringProperty).toMap()
)

private val TfOverrideDataBlock: BlockType = BlockType(
  HCL_OVERRIDE_DATA_IDENTIFIER,
  properties = listOf(TargetProperty, ValuesProperty, OverrideDuringProperty).toMap()
)

private val TfOverrideModuleBlock: BlockType = BlockType(
  HCL_OVERRIDE_MODULE_IDENTIFIER,
  properties = listOf(TargetProperty, PropertyType("outputs", Types.Object), OverrideDuringProperty).toMap()
)

// Mock blocks
private val TfMockResourceBlock: BlockType = BlockType(
  HCL_MOCK_RESOURCE_IDENTIFIER,
  1,
  properties = listOf(DefaultsProperty, OverrideDuringProperty).toMap()
)

private val TfMockDataBlock: BlockType = BlockType(
  HCL_MOCK_DATA_IDENTIFIER,
  1,
  properties = listOf(DefaultsProperty, OverrideDuringProperty).toMap()
)

private val TfMockProviderBlock: BlockType = BlockType(
  HCL_MOCK_PROVIDER_IDENTIFIER,
  1,
  properties = listOf(
    PropertyType("alias", Types.String),
    PropertyType(HCL_SOURCE_IDENTIFIER, Types.String),
    OverrideDuringProperty,
    TfMockResourceBlock,
    TfMockDataBlock,
    TfOverrideResourceBlock,
    TfOverrideDataBlock
  ).toMap()
)

internal val TfTestRootBlocks: List<BlockType> = listOf(
  TfTestBlock,
  TfTestRunBlock,
  TfTestVariablesBlock,
  TfTypeModel.AbstractProvider,
  // Mocks
  TfMockProviderBlock,
  TfMockResourceBlock,
  TfMockDataBlock,
  // Overrides
  TfOverrideResourceBlock,
  TfOverrideDataBlock,
  TfOverrideModuleBlock,
).sortedBy { it.literal }

internal val TfTestRootBlocksMap: Map<String, BlockType> = TfTestRootBlocks.associateBy { it.literal }