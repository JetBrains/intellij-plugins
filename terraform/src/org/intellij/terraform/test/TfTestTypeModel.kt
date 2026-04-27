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

internal val TfTestRootBlocks: List<BlockType> = listOf(
  TfTestBlock,
  TfTestRunBlock,
  TfTestVariablesBlock,
  TfTypeModel.AbstractProvider,
).sortedBy { it.literal }

internal val TfTestRootBlocksMap: Map<String, BlockType> = TfTestRootBlocks.associateBy { it.literal }