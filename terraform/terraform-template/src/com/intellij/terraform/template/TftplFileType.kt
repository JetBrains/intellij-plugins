// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.terraform.template

import com.intellij.openapi.fileTypes.LanguageFileType
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.hil.psi.template.TftplLanguage
import javax.swing.Icon

object TftplFileType : LanguageFileType(TftplLanguage) {

  override fun getName(): String = "Terraform Template"

  override fun getDescription(): String = TftplBundle.message("terraform.template.display.name")

  override fun getDefaultExtension(): String = "tftpl"

  override fun getIcon(): Icon = TerraformIcons.Terraform
}