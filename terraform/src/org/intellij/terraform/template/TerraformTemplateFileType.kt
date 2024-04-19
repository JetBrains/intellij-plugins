// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template

import com.intellij.openapi.fileTypes.LanguageFileType
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hil.psi.TerraformTemplateLanguage
import javax.swing.Icon

object TerraformTemplateFileType : LanguageFileType(TerraformTemplateLanguage) {

  override fun getName(): String = "Terraform Template"

  override fun getDescription(): String = HCLBundle.message("terraform.template.file.type")

  override fun getDefaultExtension(): String = "tftpl"

  override fun getIcon(): Icon = TerraformIcons.Terraform
}