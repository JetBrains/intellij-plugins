// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.deployment

import org.intellij.terraform.config.Constants.HCL_CHECK_BLOCK_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_CONDITION_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_ID_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_IMPORT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_SOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TYPE_IDENTIFIER
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.model.TfTypeModel.Companion.DescriptionProperty
import org.intellij.terraform.config.model.TfTypeModel.Companion.Locals
import org.intellij.terraform.stack.component.InputsProperty

internal val DeploymentBlock: BlockType = BlockType(
  "deployment", 1,
  properties = listOf(
    InputsProperty,
    PropertyType(DEPLOYMENT_GROUP_BLOCK, Types.String),
    PropertyType("destroy", Types.Boolean),
    PropertyType(HCL_IMPORT_IDENTIFIER, Types.Boolean)
  ).toMap()
)

internal val DeploymentGroupBlock: BlockType = BlockType(
  DEPLOYMENT_GROUP_BLOCK, 1,
  properties = listOf(
    PropertyType("auto_approve_checks", Types.Array, required = true)
  ).toMap()
)

internal val DeploymentAutoApproveBlock: BlockType = BlockType(
  "deployment_auto_approve", 1,
  properties = listOf(
    BlockType(HCL_CHECK_BLOCK_IDENTIFIER, required = true, properties = listOf(
      PropertyType(HCL_CONDITION_IDENTIFIER, Types.Boolean, required = true),
      PropertyType("reason", Types.String, required = true)
    ).toMap())
  ).toMap()
)

internal val IdentityTokenBlock: BlockType = BlockType(
  "identity_token", 1,
  properties = listOf(
    PropertyType("audience", ListType(Types.String), required = true)
  ).toMap()
)

internal val StoreBlock: BlockType = BlockType(
  "store", 2,
  properties = listOf(
    PropertyType("name", Types.String, required = true, conflictsWith = listOf(HCL_ID_IDENTIFIER)),
    // IJPL-233270 The `id` property is required if `name` is not provided
    PropertyType(HCL_ID_IDENTIFIER, Types.String, conflictsWith = listOf("name")),
    PropertyType("category", Types.String, required = true),
  ).toMap()
)

internal val PublishOutputsBlock: BlockType = BlockType(
  "publish_outputs", 1,
  properties = listOf(
    PropertyType("value", Types.Any, required = true),
    DescriptionProperty
  ).toMap()
)

internal val UpstreamInput: BlockType = BlockType(
  "upstream_input", 1,
  properties = listOf(
    PropertyType(HCL_TYPE_IDENTIFIER, Types.String, required = true),
    PropertyType(HCL_SOURCE_IDENTIFIER, Types.String, required = true)
  ).toMap()
)

internal val TfDeployRootBlocks: List<BlockType> = listOf(
  DeploymentBlock,
  DeploymentGroupBlock,
  DeploymentAutoApproveBlock,
  IdentityTokenBlock,
  StoreBlock,
  PublishOutputsBlock,
  UpstreamInput,
  Locals
)

internal val TfDeployRootBlocksMap: Map<String, BlockType> = TfDeployRootBlocks.associateBy { it.literal }

internal const val DEPLOYMENT_GROUP_BLOCK = "deployment_group"