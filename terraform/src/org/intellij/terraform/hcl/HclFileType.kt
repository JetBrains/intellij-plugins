// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl

import com.intellij.openapi.fileTypes.LanguageFileType
import org.intellij.terraform.TerraformIcons
import javax.swing.Icon

object HclFileType : LanguageFileType(HCLLanguage) {
  override fun getIcon(): Icon = TerraformIcons.HashiCorp

  override fun getDefaultExtension(): String = HCL_DEFAULT_EXTENSION

  override fun getDescription(): String = HCLBundle.message("HclFileType.description")

  override fun getName(): String = "HCL"
}

internal const val HCL_DEFAULT_EXTENSION = "hcl"