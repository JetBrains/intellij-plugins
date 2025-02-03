// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.openapi.fileTypes.LanguageFileType
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.hcl.HCLBundle
import javax.swing.Icon

object TerraformFileType : LanguageFileType(TerraformLanguage) {
  const val DEFAULT_EXTENSION: String = "tf"
  const val TFVARS_EXTENSION: String = "tfvars"

  override fun getIcon(): Icon = TerraformIcons.Terraform

  override fun getDefaultExtension(): String = DEFAULT_EXTENSION

  override fun getDescription(): String = HCLBundle.message("TerraformFileType.description")

  override fun getName(): String = "Terraform"

  override fun toString(): String = name
}