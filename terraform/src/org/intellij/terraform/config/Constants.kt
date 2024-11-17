// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.openapi.util.NlsSafe

object Constants {
  const val HAS_DYNAMIC_ATTRIBUTES: String = "__has_dynamic_attributes"
  const val TIMEOUTS: String = "__timeouts__"

  internal const val HCL_RESOURCE_IDENTIFIER: String = "resource"
  internal const val HCL_DATASOURCE_IDENTIFIER: String = "data"
  internal const val HCL_PROVIDER_IDENTIFIER: String = "provider"
  internal const val HCL_MODULE_IDENTIFIER: String = "module"
  internal const val HCL_VARIABLE_IDENTIFIER: String = "variable"
  internal const val HCL_OUTPUT_IDENTIFIER: String = "output"
  internal const val HCL_TERRAFORM_IDENTIFIER: String = "terraform"
  internal const val HCL_TERRAFORM_REQUIRED_PROVIDERS: String = "required_providers"
  internal const val HCL_LOCALS_IDENTIFIER: String = "locals"
  internal const val HCL_PROVISIONER_IDENTIFIER: String = "provisioner"
  internal const val HCL_BACKEND_IDENTIFIER: String = "backend"
  internal const val HCL_LIFECYCLE_IDENTIFIER: String = "lifecycle"
  internal const val HCL_CONNECTION_IDENTIFIER: String = "connection"
  internal const val HCL_MOVED_BLOCK_IDENTIFIER: String = "moved"
  internal const val HCL_DYNAMIC_BLOCK_IDENTIFIER: String = "dynamic"
  internal const val HCL_DYNAMIC_BLOCK_CONTENT_IDENTIFIER: String = "content"
  internal const val HCL_CLOUD_IDENTIFIER: String = "cloud"
  internal const val HCL_IMPORT_IDENTIFIER: String = "import"
  internal const val HCL_ATLAS_IDENTIFIER: String = "atlas"
  internal const val HCL_VALIDATION_IDENTIFIER: String = "validation"
  internal const val HCL_REMOVED_BLOCK_IDENTIFIER: String = "removed"
  internal const val HCL_CHECK_BLOCK_IDENTIFIER: String = "check"
  internal const val HCL_ASSERT_BLOCK_IDENTIFIER: String = "assert"
  internal const val HCL_WORKSPACES_BLOCK_IDENTIFIER: String = "workspaces"
  internal const val HCL_PRECONDITION_BLOCK_IDENTIFIER: String = "precondition"
  internal const val HCL_POSTCONDITION_BLOCK_IDENTIFIER: String = "postcondition"


  internal const val REGISTRY_DOMAIN: String = "registry.terraform.io"
  internal const val LATEST_VERSION: String = "latest"

  internal const val PROVIDER_VERSION: String = "version"

  internal const val TF_FMT: @NlsSafe String = "Terraform/OpenTofu format"

  internal val OFFICIAL_PROVIDERS_NAMESPACE: Set<String> = setOf("hashicorp", "builtin")
}