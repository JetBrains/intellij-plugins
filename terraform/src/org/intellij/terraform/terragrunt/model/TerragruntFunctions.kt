// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.model

import org.intellij.terraform.config.model.*

// Terragrunt built-in helper functions
// Source: https://terragrunt.gruntwork.io/docs/reference/hcl/functions
internal val TerragruntFunctions: List<TfFunction> = listOf(
  TfFunction(
    name = "find_in_parent_folders",
    returnType = Types.String,
    arguments = arrayOf(
      Argument(Types.String, "filename"),
      VariadicArgument(Types.String, "fallback")
    )
  ),

  TfFunction(
    name = "path_relative_to_include",
    returnType = Types.String
  ),

  TfFunction(
    name = "path_relative_from_include",
    returnType = Types.String
  ),

  TfFunction(
    name = "get_env",
    returnType = Types.String,
    arguments = arrayOf(
      Argument(Types.String, "NAME"),
      VariadicArgument(Types.String, "DEFAULT")
    )
  ),

  TfFunction(
    name = "get_platform",
    returnType = Types.String
  ),

  TfFunction(
    name = "get_repo_root",
    returnType = Types.String
  ),

  TfFunction(
    name = "get_path_from_repo_root",
    returnType = Types.String
  ),

  TfFunction(
    name = "get_path_to_repo_root",
    returnType = Types.String
  ),

  TfFunction(
    name = "get_terragrunt_dir",
    returnType = Types.String
  ),

  TfFunction(
    name = "get_parent_terragrunt_dir",
    returnType = Types.String
  ),

  TfFunction(
    name = "get_original_terragrunt_di",
    returnType = Types.String
  ),

  TfFunction(
    name = "get_terraform_commands_that_need_vars",
    returnType = ListType(Types.String)
  ),

  TfFunction(
    name = "get_terraform_commands_that_need_input",
    returnType = ListType(Types.String)
  ),

  TfFunction(
    name = "get_terraform_commands_that_need_locking",
    returnType = ListType(Types.String)
  ),

  TfFunction(
    name = "get_terraform_commands_that_need_parallelism",
    returnType = ListType(Types.String)
  ),

  TfFunction(
    name = "get_aws_account_alias",
    returnType = Types.String
  ),

  TfFunction(
    name = "get_aws_account_id",
    returnType = Types.String
  ),

  TfFunction(
    name = "get_aws_caller_identity_arn",
    returnType = Types.String
  ),

  TfFunction(
    name = "get_terraform_command",
    returnType = Types.String
  ),

  TfFunction(
    name = "get_terraform_cli_args",
    returnType = ListType(Types.String)
  ),

  TfFunction(
    name = "get_default_retryable_errors",
    returnType = ListType(Types.String)
  ),

  TfFunction(
    name = "get_aws_caller_identity_user_id",
    returnType = Types.String
  ),

  TfFunction(
    name = "run_cmd",
    returnType = Types.String,
    arguments = arrayOf(
      Argument(Types.String, "command"),
      VariadicArgument(Types.String, "arg"),
    )
  ),

  TfFunction(
    name = "read_terragrunt_config",
    returnType = Types.Object,
    arguments = arrayOf(Argument(Types.String, "config_path"))
  ),

  TfFunction(
    name = "sops_decrypt_file",
    returnType = Types.String,
    arguments = arrayOf(Argument(Types.String, "file_path"))
  ),

  TfFunction(
    name = "get_terragrunt_source_cli_flag",
    returnType = Types.String
  ),

  TfFunction(
    name = "read_tfvars_file",
    returnType = Types.Object,
    arguments = arrayOf(Argument(Types.String, "file_path"))
  ),

  TfFunction(
    name = "mark_as_read",
    returnType = Types.String,
    arguments = arrayOf(Argument(Types.String, "file_path"))
  ),

  TfFunction(
    name = "constraint_check",
    returnType = Types.Boolean,
    arguments = arrayOf(
      Argument(Types.String, "version"),
      Argument(Types.String, "constraint")
    )
  )
)