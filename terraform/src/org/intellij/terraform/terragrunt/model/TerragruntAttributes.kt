// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.model

import org.intellij.terraform.config.Constants.HCL_INPUTS_IDENTIFIER
import org.intellij.terraform.config.model.ListType
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.Types

// https://terragrunt.gruntwork.io/docs/reference/hcl/attributes/
internal val TerragruntAttributes: List<PropertyType> = listOf(
  PropertyType(HCL_INPUTS_IDENTIFIER, Types.Object),
  PropertyType("download_dir", Types.String),
  PropertyType("prevent_destroy", Types.Boolean),
  PropertyType("skip", Types.Boolean),
  PropertyType("iam_role", Types.String),
  PropertyType("iam_assume_role_duration", Types.Number),
  PropertyType("iam_assume_role_session_name", Types.String),
  PropertyType("iam_web_identity_token", Types.String),
  PropertyType("terraform_binary", Types.String),
  PropertyType("terraform_version_constraint", Types.String),
  PropertyType("terragrunt_version_constraint", Types.String),
  PropertyType("retryable_errors", ListType(Types.String))
)