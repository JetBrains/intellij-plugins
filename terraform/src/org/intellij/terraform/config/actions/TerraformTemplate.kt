// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
  val defaultFileName: String? = null
) {
  EMPTY("Empty File", HCLBundle.message("action.new.empty.terraform.file.description"), TerraformIcons.Terraform),
  MAIN("Main", HCLBundle.message("action.new.template.terraform.file.description"), TerraformIcons.Terraform),
  OUTPUTS("Outputs", HCLBundle.message("action.new.outputs.terraform.file.description"), TerraformIcons.Terraform, "outputs"),
  VARIABLES("Variables", HCLBundle.message("action.new.variables.terraform.file.description"), TerraformIcons.Terraform, "variables"),
  TERRAGRUNT("Terragrunt File", HCLBundle.message("action.new.terragrunt.file.description"), TerraformIcons.Terragrunt, "terragrunt"),
  TERRAGRUNT_STACK("Terragrunt Stack", HCLBundle.message("action.new.terragrunt.stack.file.description"), TerraformIcons.Terragrunt, "terragrunt.stack");
}
