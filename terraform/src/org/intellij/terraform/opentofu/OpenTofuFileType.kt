// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu

import com.intellij.openapi.fileTypes.LanguageFileType
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.hcl.HCLBundle
import org.jetbrains.annotations.Nls
import javax.swing.Icon

object OpenTofuFileType: LanguageFileType(TerraformLanguage) {

  override fun getIcon(): Icon = TerraformIcons.Opentofu

  override fun getDefaultExtension(): String = DEFAULT_EXTENSION

  override fun getDescription(): String = HCLBundle.message("opentofu.filetype.description")

  override fun getName(): String = "OpenTofu"

  override fun toString(): String = OpenTofuFileType.name

  override fun getDisplayName(): @Nls String {
    return HCLBundle.message("opentofu.display.name")
  }
}

private const val DEFAULT_EXTENSION: String = "tofu"