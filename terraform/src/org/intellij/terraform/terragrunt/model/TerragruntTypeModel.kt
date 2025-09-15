// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.model

import org.intellij.terraform.config.Constants.HCL_BACKEND_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_LOCALS_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PATH_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_SOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VERSION_IDENTIFIER
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.hcl.HCL_DEFAULT_EXTENSION
import org.intellij.terraform.terragrunt.TERRAGRUNT_COMMANDS
import org.intellij.terraform.terragrunt.TERRAGRUNT_EXECUTE

// Terragrunt `terraform` block schema based on Terragrunt documentation
// https://terragrunt.gruntwork.io/docs/reference/config-blocks-and-attributes/#terraform
internal val TfBlockInTerragrunt: BlockType = BlockType(
  literal = HCL_TERRAFORM_IDENTIFIER,
  properties = listOf(
    PropertyType(HCL_SOURCE_IDENTIFIER, Types.String),
    PropertyType("include_in_copy", ListType(Types.String)),
    PropertyType("exclude_from_copy", ListType(Types.String)),
    //TODO: default value is "true"
    PropertyType("copy_terraform_lock_file", Types.Boolean),

    // Extra arguments block
    BlockType("extra_arguments", properties = listOf(
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

internal val RemoteStateBlock: BlockType = BlockType(
  literal = "remote_state",
  properties = listOf(
    PropertyType(HCL_BACKEND_IDENTIFIER, Types.String, required = true, optional = false),
    PropertyType("disable_init", Types.Boolean),
    PropertyType("disable_dependency_optimization", Types.Boolean),
    BlockType("generate", properties = listOf(
      PropertyType(HCL_PATH_IDENTIFIER, Types.String, required = true, optional = false),
      createIfExistsProperty(isRequired = true, isOptional = false),
    ).toMap()),
    BlockType("config"),
    BlockType("encryption")
  ).toMap()
)

internal val IncludeBlock: BlockType = BlockType(
  literal = "include", args = 1,
  properties = listOf(
    PropertyType(HCL_PATH_IDENTIFIER, Types.String, required = true, optional = false),
    PropertyType("expose", Types.Boolean),
    PropertyType("merge_strategy", Types.String, hint = SimpleValueHint("no_merge", "shallow", "deep"))
  ).toMap()
)

internal val LocalsTerragrunt: BlockType = BlockType(HCL_LOCALS_IDENTIFIER)

internal val DependencyBlock: BlockType = BlockType(
  "dependency", args = 1,
  properties = listOf(
    PropertyType("config_path", Types.String, required = true, optional = false),
    // TODO: default value is "true"
    PropertyType("enabled", Types.Boolean),
    PropertyType("skip_outputs", Types.Boolean),
    BlockType("mock_outputs"),
    PropertyType("mock_outputs_allowed_terraform_commands", ListType(Types.String)),
    PropertyType("mock_outputs_merge_strategy_with_state", Types.String, hint = SimpleValueHint("no_merge", "shallow", "deep_map_only"))
  ).toMap()
)

internal val DependenciesBlock: BlockType = BlockType(
  "dependencies",
  properties = listOf(PropertyType("paths", ListType(Types.String), required = true, optional = false)).toMap()
)

internal val GenerateBlock: BlockType = BlockType(
  "generate", args = 1,
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

internal val EngineBlock: BlockType = BlockType(
  "engine",
  properties = listOf(
    PropertyType(HCL_SOURCE_IDENTIFIER, Types.String, required = true, optional = false),
    PropertyType(HCL_VERSION_IDENTIFIER, Types.String),
    PropertyType("type", Types.String, hint = SimpleValueHint("rpc")),
    PropertyType("meta", Types.Object)
  ).toMap()
)

internal val FeatureBlock: BlockType = BlockType(
  "feature", args = 1,
  properties = listOf(PropertyType(HCL_DEFAULT_EXTENSION, Types.String, required = true, optional = false)).toMap()
)

internal val ExcludeBlock: BlockType = BlockType(
  "exclude",
  properties = listOf(
    PropertyType("if", Types.Boolean),
    PropertyType("actions", ListType(Types.String), hint = SimpleValueHint("plan", "apply", "all", "all_except_output")),
    PropertyType("exclude_dependencies", Types.Boolean),
    PropertyType("no_run", Types.Boolean)
  ).toMap()
)

internal val ErrorsBlock: BlockType = BlockType(
  "errors",
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
  TfBlockInTerragrunt,
  RemoteStateBlock,
  IncludeBlock,
  LocalsTerragrunt,
  DependencyBlock,
  DependenciesBlock,
  GenerateBlock,
  EngineBlock,
  FeatureBlock,
  ExcludeBlock,
  ErrorsBlock
)

internal val StackRootBlocks: List<BlockType> = listOf()

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
