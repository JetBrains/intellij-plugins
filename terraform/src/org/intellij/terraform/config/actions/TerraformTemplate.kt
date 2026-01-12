// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.openapi.util.NlsContexts
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.hcl.HCLBundle
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

@ApiStatus.Internal
enum class TerraformTemplate(
  @param:NonNls val templateName: String,
  @NlsContexts.ListItem val title: String,
  val icon: Icon,
  val fileNamePattern: String? = null,
) {
  CONFIG("Config", HCLBundle.message("action.new.terraform.file.description"), TerraformIcons.Terraform),
  COMPONENT("Component", HCLBundle.message("action.new.component.file.description"), TerraformIcons.Terraform, "tfcomponent"),
  DEPLOY("Deploy", HCLBundle.message("action.new.deploy.file.description"), TerraformIcons.Terraform, "tfdeploy"),

  OPEN_TOFU("OpenTofu", HCLBundle.message("action.new.tofu.file.description"), TerraformIcons.Opentofu),

  TERRAGRUNT("Terragrunt Config", HCLBundle.message("action.new.terragrunt.file.description"), TerraformIcons.Terragrunt, "terragrunt"),
  TERRAGRUNT_STACK("Terragrunt Stack", HCLBundle.message("action.new.terragrunt.stack.file.description"), TerraformIcons.Terragrunt, "terragrunt.stack");
}
