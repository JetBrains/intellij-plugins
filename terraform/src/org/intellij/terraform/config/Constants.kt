// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.openapi.util.NlsSafe

internal object Constants {
  const val HAS_DYNAMIC_ATTRIBUTES: String = "__has_dynamic_attributes"
  const val TIMEOUTS: String = "__timeouts__"

  const val HCL_RESOURCE_IDENTIFIER: String = "resource"
  const val HCL_DATASOURCE_IDENTIFIER: String = "data"
  const val HCL_PROVIDER_IDENTIFIER: String = "provider"
  const val HCL_MODULE_IDENTIFIER: String = "module"
  const val HCL_VARIABLE_IDENTIFIER: String = "variable"
  const val HCL_OUTPUT_IDENTIFIER: String = "output"
  const val HCL_TERRAFORM_IDENTIFIER: String = "terraform"
  const val HCL_TERRAFORM_REQUIRED_PROVIDERS: String = "required_providers"
  const val HCL_LOCALS_IDENTIFIER: String = "locals"
  const val HCL_PROVISIONER_IDENTIFIER: String = "provisioner"
  const val HCL_BACKEND_IDENTIFIER: String = "backend"
  const val HCL_LIFECYCLE_IDENTIFIER: String = "lifecycle"
  const val HCL_CONNECTION_IDENTIFIER: String = "connection"
  const val HCL_MOVED_BLOCK_IDENTIFIER: String = "moved"
  const val HCL_DYNAMIC_BLOCK_IDENTIFIER: String = "dynamic"
  const val HCL_DYNAMIC_BLOCK_CONTENT_IDENTIFIER: String = "content"
  const val HCL_CLOUD_IDENTIFIER: String = "cloud"
  const val HCL_IMPORT_IDENTIFIER: String = "import"
  const val HCL_ATLAS_IDENTIFIER: String = "atlas"
  const val HCL_VALIDATION_IDENTIFIER: String = "validation"
  const val HCL_REMOVED_BLOCK_IDENTIFIER: String = "removed"
  const val HCL_CHECK_BLOCK_IDENTIFIER: String = "check"
  const val HCL_ASSERT_BLOCK_IDENTIFIER: String = "assert"
  const val HCL_WORKSPACES_BLOCK_IDENTIFIER: String = "workspaces"
  const val HCL_PRECONDITION_BLOCK_IDENTIFIER: String = "precondition"
  const val HCL_POSTCONDITION_BLOCK_IDENTIFIER: String = "postcondition"
  const val HCL_EPHEMERAL_IDENTIFIER: String = "ephemeral"
  const val HCL_SELF_IDENTIFIER: String = "self"
  const val HCL_PATH_IDENTIFIER: String = "path"
  const val HCL_LOCAL_IDENTIFIER: String = "local"
  const val HCL_VAR_IDENTIFIER: String = "var"
  const val HCL_DEPENDS_ON_IDENTIFIER: String = "depends_on"
  const val HCL_FOR_EACH_IDENTIFIER: String = "for_each"
  const val HCL_COUNT_IDENTIFIER: String = "count"
  const val HCL_SOURCE_IDENTIFIER: String = "source"
  const val HCL_VERSION_IDENTIFIER: String = "version"
  const val HCL_DEFAULT_IDENTIFIER: String = "default"
  const val HCL_INPUTS_IDENTIFIER: String = "inputs"
  const val HCL_TYPE_IDENTIFIER: String = "type"
  const val HCL_CONFIG_IDENTIFIER: String = "config"
  const val HCL_CONDITION_IDENTIFIER: String = "condition"
  const val HCL_ID_IDENTIFIER: String = "id"
  const val HCL_STACK_IDENTIFIER: String = "stack"
  const val HCL_COMPONENT_IDENTIFIER = "component"
  const val HCL_PROVIDERS_IDENTIFIER = "providers"

  const val REGISTRY_DOMAIN: String = "registry.terraform.io"
  const val LATEST_VERSION: String = "latest"

  const val TF_FMT: @NlsSafe String = "Terraform/OpenTofu format"

  val OfficialProvidersNamespace: Set<String> = setOf("hashicorp", "builtin")
}