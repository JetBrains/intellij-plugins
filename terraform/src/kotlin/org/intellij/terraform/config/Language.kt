// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.lang.Language
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.HILCompatibleLanguage

object TerraformLanguage : Language(HCLLanguage, "HCL-Terraform"), HILCompatibleLanguage {
  override fun getDisplayName(): String {
    return "Terraform Config"
  }
}