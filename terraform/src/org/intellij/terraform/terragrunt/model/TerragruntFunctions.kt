// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.model

import org.intellij.terraform.config.model.*

// Terragrunt built-in helper functions
// Source: https://terragrunt.gruntwork.io/docs/reference/hcl/functions
internal val TerragruntFunctions: List<TfFunction> = listOf(
  TfFunction(
    "find_in_parent_folders", Types.String,
    arguments = arrayOf(
      Argument(Types.String, "filename"),
      VariadicArgument(Types.String, "fallback")
    )
  ),
  TfFunction("path_relative_to_include", Types.String),
  TfFunction("path_relative_from_include", Types.String),
  TfFunction(
    "get_env", Types.String,
    arguments = arrayOf(
      Argument(Types.String, "NAME"),
      VariadicArgument(Types.String, "DEFAULT")
    )
  ),
  TfFunction("get_platform", Types.String),
  TfFunction("get_repo_root", Types.String),
  TfFunction("get_path_from_repo_root", Types.String),
  TfFunction("get_path_to_repo_root", Types.String),
  TfFunction("get_terragrunt_dir", Types.String),
  TfFunction("get_parent_terragrunt_dir", Types.String),
  TfFunction("get_original_terragrunt_di", Types.String),
  TfFunction("get_terraform_commands_that_need_vars", ListType(Types.String)),
  TfFunction("get_terraform_commands_that_need_input", ListType(Types.String)),
  TfFunction("get_terraform_commands_that_need_locking", ListType(Types.String)),
  TfFunction("get_terraform_commands_that_need_parallelism", ListType(Types.String)),
  TfFunction("get_aws_account_alias", Types.String),
  TfFunction("get_aws_account_id", Types.String),
  TfFunction("get_aws_caller_identity_arn", Types.String),
  TfFunction("get_terraform_command", Types.String),
  TfFunction("get_terraform_cli_args", ListType(Types.String)),
  TfFunction("get_default_retryable_errors", ListType(Types.String)),
  TfFunction("get_aws_caller_identity_user_id", Types.String),
  TfFunction(
    "run_cmd", Types.String,
    arguments = arrayOf(
      Argument(Types.String, "command"),
      VariadicArgument(Types.String, "arg"),
    )
  ),
  TfFunction(
    "read_terragrunt_config", Types.Object,
    arguments = arrayOf(Argument(Types.String, "config_path"))
  ),
  TfFunction(
    "sops_decrypt_file", Types.String,
    arguments = arrayOf(Argument(Types.String, "file_path"))
  ),
  TfFunction("get_terragrunt_source_cli_flag", Types.String),
  TfFunction(
    "read_tfvars_file", Types.Object,
    arguments = arrayOf(Argument(Types.String, "file_path"))
  ),

  TfFunction(
    "mark_as_read", Types.String,
    arguments = arrayOf(Argument(Types.String, "file_path"))
  ),
  TfFunction(
    "constraint_check", Types.Boolean,
    arguments = arrayOf(
      Argument(Types.String, "version"),
      Argument(Types.String, "constraint")
    )
  )
)