// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.model

import org.intellij.terraform.config.Constants.HCL_BACKEND_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_DEFAULT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_LOCALS_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PATH_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_SOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VERSION_IDENTIFIER
import org.intellij.terraform.config.model.*
import org.intellij.terraform.terragrunt.*

// Terragrunt `terraform` block schema based on Terragrunt documentation
// https://terragrunt.gruntwork.io/docs/reference/config-blocks-and-attributes/#terraform
internal val TfBlockType: BlockType = BlockType(
  literal = HCL_TERRAFORM_IDENTIFIER,
  properties = listOf(
    PropertyType(HCL_SOURCE_IDENTIFIER, Types.String),
    PropertyType("include_in_copy", ListType(Types.String)),
    PropertyType("exclude_from_copy", ListType(Types.String)),
    //TODO: default value is "true"
    PropertyType("copy_terraform_lock_file", Types.Boolean),

    // Extra arguments block: https://terragrunt.gruntwork.io/docs/features/extra-arguments
    BlockType("extra_arguments", args = 1, properties = listOf(
      PropertyType("arguments", ListType(Types.String), required = true, optional = false),
      PropertyType(TERRAGRUNT_COMMANDS, ListType(Types.String), required = true, optional = false),
      PropertyType("env_vars", MapType(Types.String)),
      PropertyType("required_var_files", ListType(Types.String)),
      PropertyType("optional_var_files", ListType(Types.String))
    ).toMap()),

    // Hook blocks: before_hook, after_hook and error_hook
    BlockType("before_hook", properties = createHooksProperties()),
    BlockType("after_hook", properties = createHooksProperties()),
    BlockType("error_hook", properties = listOf(
      PropertyType("on_errors", ListType(Types.String), required = true, optional = false),
      PropertyType(TERRAGRUNT_EXECUTE, ListType(Types.String), required = true, optional = false),
      PropertyType(TERRAGRUNT_COMMANDS, ListType(Types.String))
    ).toMap())
  ).toMap()
)

// https://terragrunt.gruntwork.io/docs/reference/hcl/blocks/#remote_state
internal val RemoteStateBlockType: BlockType = BlockType(
  literal = TERRAGRUNT_REMOTE_STATE,
  properties = listOf(
    PropertyType(HCL_BACKEND_IDENTIFIER, Types.String, required = true, optional = false),
    PropertyType("config", Types.Object, required = true, optional = false),
    PropertyType("disable_init", Types.Boolean),
    PropertyType("disable_dependency_optimization", Types.Boolean),
    PropertyType(
      "generate", Types.Object,
      // IJPL-211118 Support predefined properties for PropertyType
      // properties = listOf(
      //   PropertyType(HCL_PATH_IDENTIFIER, Types.String, required = true, optional = false),
      //   createIfExistsProperty(isRequired = true, isOptional = false)
      // ).toMap()
    ),
    PropertyType("encryption", Types.Object),
  ).toMap()
)

internal val IncludeBlockType: BlockType = BlockType(
  literal = TERRAGRUNT_INCLUDE, args = 1,
  properties = listOf(
    PropertyType(HCL_PATH_IDENTIFIER, Types.String, required = true, optional = false),
    PropertyType("expose", Types.Boolean),
    PropertyType("merge_strategy", Types.String, hint = SimpleValueHint("no_merge", "shallow", "deep"))
  ).toMap()
)

internal val LocalsBlockType: BlockType = BlockType(HCL_LOCALS_IDENTIFIER)

// https://terragrunt.gruntwork.io/docs/reference/hcl/blocks/#dependency
internal val DependencyBlockType: BlockType = BlockType(
  TERRAGRUNT_DEPENDENCY, args = 1,
  properties = listOf(
    PropertyType("config_path", Types.String, required = true, optional = false),
    // TODO: default value is "true"
    PropertyType("enabled", Types.Boolean),
    PropertyType("skip_outputs", Types.Boolean),
    PropertyType("mock_outputs", Types.Object),
    PropertyType("mock_outputs_allowed_terraform_commands", ListType(Types.String)),
    PropertyType("mock_outputs_merge_strategy_with_state", Types.String, hint = SimpleValueHint("no_merge", "shallow", "deep_map_only"))
  ).toMap()
)

internal val DependenciesBlockType: BlockType = BlockType(
  TERRAGRUNT_DEPENDENCIES,
  properties = listOf(PropertyType("paths", ListType(Types.String), required = true, optional = false)).toMap()
)

internal val GenerateBlockType: BlockType = BlockType(
  TERRAGRUNT_GENERATE, args = 1,
  properties = listOf(
    PropertyType(HCL_PATH_IDENTIFIER, Types.String, required = true, optional = false),
    PropertyType("contents", Types.String, required = true, optional = false),

    createIfExistsProperty(),
    //TODO: default value is "skip"
    PropertyType("if_disabled", Types.String, hint = SimpleValueHint("remove", "remove_terragrunt", "skip")),
    //TODO: default value is "#"
    PropertyType("comment_prefix", Types.String, hint = SimpleValueHint("#", "//")),
    //TODO: default value is "false"
    PropertyType("disable_signature", Types.Boolean),
    PropertyType("disable", Types.Boolean)
  ).toMap()
)

internal val EngineBlockType: BlockType = BlockType(
  TERRAGRUNT_ENGINE,
  properties = listOf(
    PropertyType(HCL_SOURCE_IDENTIFIER, Types.String, required = true, optional = false),
    PropertyType(HCL_VERSION_IDENTIFIER, Types.String),
    PropertyType("type", Types.String, hint = SimpleValueHint("rpc")),
    PropertyType("meta", Types.Object)
  ).toMap()
)

internal val FeatureBlockType: BlockType = BlockType(
  TERRAGRUNT_FEATURE, args = 1,
  properties = listOf(PropertyType(HCL_DEFAULT_IDENTIFIER, Types.String, required = true, optional = false)).toMap()
)

internal val ExcludeBlockType: BlockType = BlockType(
  TERRAGRUNT_EXCLUDE,
  properties = listOf(
    PropertyType("if", Types.Boolean),
    PropertyType("actions", ListType(Types.String), hint = SimpleValueHint("plan", "apply", "all", "all_except_output")),
    PropertyType("exclude_dependencies", Types.Boolean),
    PropertyType("no_run", Types.Boolean)
  ).toMap()
)

internal val ErrorsBlockType: BlockType = BlockType(
  TERRAGRUNT_ERRORS,
  properties = listOf(
    BlockType(
      "retry", args = 1,
      properties = listOf(
        PropertyType("retryable_errors", ListType(Types.String), required = true, optional = false),
        PropertyType("max_attempts", Types.Number),
        PropertyType("sleep_interval_sec", Types.Number)
      ).toMap()),
    BlockType(
      "ignore", args = 1,
      properties = listOf(
        PropertyType("ignorable_errors", ListType(Types.String), required = true, optional = false),
        PropertyType("message", Types.String),
        PropertyType("signals", Types.Object)
      ).toMap())
  ).toMap()
)

internal val TerragruntRootBlocks: List<BlockType> = listOf(
  TfBlockType,
  RemoteStateBlockType,
  IncludeBlockType,
  LocalsBlockType,
  DependencyBlockType,
  DependenciesBlockType,
  GenerateBlockType,
  EngineBlockType,
  FeatureBlockType,
  ExcludeBlockType,
  ErrorsBlockType
)
internal val TerragruntRootBlocksMap: Map<String, BlockType> = TerragruntRootBlocks.associateBy { it.literal }

internal val UnitBlockType: BlockType = BlockType(TERRAGRUNT_UNIT, args = 1, properties = createStacksProperties())
internal val StackBlockType: BlockType = BlockType(TERRAGRUNT_STACK, args = 1, properties = createStacksProperties())

internal val StackRootBlocks: List<BlockType> = listOf(UnitBlockType, StackBlockType)
internal val StackRootBlocksMap: Map<String, BlockType> = StackRootBlocks.associateBy { it.literal }

private fun createHooksProperties(): Map<String, PropertyOrBlockType> = listOf(
  PropertyType(TERRAGRUNT_COMMANDS, ListType(Types.String), required = true, optional = false),
  PropertyType(TERRAGRUNT_EXECUTE, ListType(Types.String), required = true, optional = false),
  PropertyType("working_dir", Types.String),
  //TODO: default value is "false"
  PropertyType("run_on_error", Types.Boolean),
  PropertyType("suppress_stdout", Types.Boolean),
  PropertyType("if", Types.Boolean)
).toMap()

private fun createIfExistsProperty(isRequired: Boolean = false, isOptional: Boolean = true): PropertyType = PropertyType(
  "if_exists", Types.String,
  hint = SimpleValueHint("overwrite", "overwrite_terragrunt", "skip", "error"),
  required = isRequired, optional = isOptional
)

private fun createStacksProperties(): Map<String, PropertyOrBlockType> = listOf(
  PropertyType(HCL_SOURCE_IDENTIFIER, Types.String, required = true, optional = false),
  PropertyType(HCL_PATH_IDENTIFIER, Types.String, required = true, optional = false),
  PropertyType("values", Types.Object),
  PropertyType("no_dot_terragrunt_stack", Types.Boolean),
  PropertyType("no_validation", Types.Boolean)
).toMap()