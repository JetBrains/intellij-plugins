// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.openapi.util.NlsSafe
import org.intellij.terraform.hcl.HCLBundle
import org.jetbrains.annotations.Nls

internal enum class TfMainCommand(
  val id: String,
  val title: @Nls String,
  val command: @NlsSafe String
) {
  INIT("Terraform Init", HCLBundle.message("terraform.run.configuration.init.name.suffix"), "init"),
  VALIDATE("Terraform Validate", HCLBundle.message("terraform.run.configuration.validate.name.suffix"), "validate"),
  PLAN("Terraform Plan", HCLBundle.message("terraform.run.configuration.plan.name.suffix"), "plan"),
  APPLY("Terraform Apply", HCLBundle.message("terraform.run.configuration.apply.name.suffix"), "apply"),
  DESTROY("Terraform Destroy", HCLBundle.message("terraform.run.configuration.destroy.name.suffix"), "destroy"),
  CUSTOM("Terraform", "", "")
}